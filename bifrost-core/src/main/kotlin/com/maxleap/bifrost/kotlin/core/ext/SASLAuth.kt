package com.maxleap.bifrost.kotlin.core.ext

import com.maxleap.bifrost.kotlin.core.AuthorException
import com.maxleap.bifrost.kotlin.core.Endpoint
import com.maxleap.bifrost.kotlin.core.model.OpBase
import com.maxleap.bifrost.kotlin.core.model.Reply
import com.maxleap.bifrost.kotlin.core.model.SASLModel
import com.maxleap.bifrost.kotlin.core.utils.Do
import com.mongodb.AuthenticationMechanism
import com.mongodb.internal.authentication.NativeAuthenticationHelper
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.binary.StringUtils
import org.bson.Document
import org.bson.types.Binary
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles
import java.security.InvalidKeyException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.spec.InvalidKeySpecException
import javax.crypto.Mac
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.xor


/**
 * Created by.
 * User: ben
 * Date: 24/07/2017
 * Time: 12:25 PM
 * Email:benkris1@126.com
 * doc https://www.mongodb.com/blog/post/improved-password-based-authentication-mongodb-30-scram-explained-part-1?jmp=docs&_ga=2.113628933.303872216.1498450526-215400923.1486350235
 */
class SASLAuth(private val endpoint: Endpoint,
               val success: Do? = null,
               val failed: Do? = null) {
  private var step = 0
  private var author = false
  private var base64Codec: Base64
  private var clientFirstMessageBare: String? = null
  private var salt: String? = null
  private var combinedNonce: String? = null
  private var serverSignature: ByteArray? = null

  init {
    base64Codec = Base64()
  }

  fun auth(saslModel: SASLModel) {
    synchronized(this) {
      try {
        val cx = saslModel.toModel()
        when (this.step) {
          0 -> {
            if (cx is SASLModel.SASLStart) {
              if (AuthenticationMechanism.SCRAM_SHA_1.mechanismName.equals(cx.mechanism)) {
                /**
                 * TODO
                 * 验证用户名 密码
                 */
                this.endpoint.write(Reply(header = saslModel.op.msgHeader,
                  documents = arrayOf(buildDoc(computeStartSaslMessage(cx.payload))))
                  .toBuffer())
                step = 1
              } else {
                throw  AuthorException("UnSupport mechanism: ${cx.mechanism}")
              }

            } else {
              throw  AuthorException("invalid SCRAM-SHA-1 auth when start!")
            }

          }
          1 -> {
            if (cx is SASLModel.SASLContinue) {

              this.endpoint.write(Reply(header = saslModel.op.msgHeader,documents = arrayOf(buildDoc(computeContinueSaslMessage_1(cx.payload)))).toBuffer())
              step = 2

            } else {
              throw  AuthorException("invalid SCRAM-SHA-1 auth when saslContinue!")
            }
          }
          2 -> {
            if (cx is SASLModel.SASLContinue) {

              this.endpoint.write(Reply(header = saslModel.op.msgHeader,documents = arrayOf(buildDoc(computeContinueSaslMessage_2(cx.payload)))).toBuffer())
              step = 0
              author = true
            } else {
              throw  AuthorException("invalid SCRAM-SHA-1 auth when saslContinue!")
            }
          }
          else -> throw UnsupportedOperationException("Not valid auth step: ${this.step}.")
        }
        this.success?.invoke()
      } catch (e: Throwable) {
        step = 0
        logger.error(e.message, e)
        failResponse(saslModel.op, e)
        this.failed?.invoke()
      }
    }
  }

  fun pass():Boolean = author

  private fun failResponse(op: OpBase, e: Throwable): Unit {
    this.endpoint.write(Reply.errorReply(op.msgHeader,18,e.message?:"Authentication Failed.").toBuffer())
  }

  private fun buildDoc(payload: ByteArray):Document {
    val doc = Document()
    doc.put("conversationId", 1)
    if (step == 2) {
      doc.put("done", true)
    } else {
      doc.put("done", false)
    }
    doc.put("payload", Binary(payload))
    doc.put("ok", 1.0)
    return doc
  }

  private fun computeStartSaslMessage(payload: ByteArray): ByteArray {
    val responsePayload = StringBuilder()
    this.clientFirstMessageBare = StringUtils.newStringUtf8(payload).substring(3)
    this.salt = this.base64Codec.encodeToString(generate(RANDOM_LENGTH))
    this.combinedNonce = getClientNonce(this.clientFirstMessageBare!!) + salt
    responsePayload.append("r=")
      .append(combinedNonce)
      .append(",s=")
      .append(salt)
      .append(",i=")
      .append(DEFAULT_ITERATE)
    return StringUtils.getBytesUtf8(responsePayload.toString())
  }

  private fun computeContinueSaslMessage_1(payload: ByteArray): ByteArray {
    if (auth(payload)) {
      return ("v=" + this.base64Codec.encodeToString(serverSignature)).toByteArray()
    }
    throw AuthorException("Authentication Failed.")
  }

  private fun computeContinueSaslMessage_2(payload: ByteArray): ByteArray {
    return "Nothing".toByteArray(Charsets.UTF_8)
  }

  private fun auth(payload: ByteArray): Boolean {
    val channelBinding = "c=" + this.base64Codec.encodeToString(StringUtils.getBytesUtf8(GS2_HEADER))
    val nonce = "r=" + combinedNonce
    val clientFinalMessageWithoutProof = channelBinding + "," + nonce

    val saltedPassword = hi(
      NativeAuthenticationHelper.createAuthenticationHash("maxleap", "maxleap10086".toCharArray()),
      this.base64Codec.decode(salt),
      DEFAULT_ITERATE
    )
    val clientKey = hmac(saltedPassword, "Client Key")
    val storedKey = h(clientKey)

    val fistResponse = StringBuilder()
    fistResponse.append("r=")
      .append(this.combinedNonce)
      .append(",s=")
      .append(this.salt)
      .append(",i=")
      .append(DEFAULT_ITERATE)

    val authMessage = this.clientFirstMessageBare + "," + fistResponse + "," + clientFinalMessageWithoutProof
    val clientSignature = hmac(storedKey, authMessage)
    val clientProof = xor(clientKey, clientSignature)
    val serverKey = hmac(saltedPassword, "Server Key")
    this.serverSignature = hmac(serverKey, authMessage)

    val proof = "p=" + this.base64Codec.encodeToString(clientProof)
    val message = clientFinalMessageWithoutProof + "," + proof
    return StringUtils.newStringUtf8(payload).equals(message)

  }


  private fun getClientNonce(clientFirstMessage: String): String {
    val pairs = clientFirstMessage.split(",")
    return pairs[pairs.size - 1].substring(2)
  }


  companion object {
    private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
    private val GS2_HEADER = "n,,"
    private val DEFAULT_ITERATE = 10000
    private val RANDOM_LENGTH = 16

    private fun hi(password: String, salt: ByteArray, iterations: Int): ByteArray {
      val spec = PBEKeySpec(password.toCharArray(), salt, iterations, 20 * 8 /* 20 bytes */)

      val keyFactory: SecretKeyFactory
      try {
        keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
      } catch (e: NoSuchAlgorithmException) {
        throw AuthorException("Unable to findUsername PBKDF2WithHmacSHA1.", e)
      }

      try {
        return keyFactory.generateSecret(spec).encoded
      } catch (e: InvalidKeySpecException) {
        throw AuthorException("Invalid key spec for PBKDC2WithHmacSHA1.", e)
      }
    }

    private fun hmac(bytes: ByteArray, key: String): ByteArray {
      val signingKey = SecretKeySpec(bytes, "HmacSHA1")

      val mac: Mac
      try {
        mac = Mac.getInstance("HmacSHA1")
      } catch (e: NoSuchAlgorithmException) {
        throw AuthorException("Could not findUsername HmacSHA1.", e)
      }

      try {
        mac.init(signingKey)
      } catch (e: InvalidKeyException) {
        throw AuthorException("Could not initialize mac.", e)
      }

      return mac.doFinal(StringUtils.getBytesUtf8(key))
    }

    private fun xor(a: ByteArray, b: ByteArray): ByteArray {
      val result = ByteArray(a.size)

      for (i in a.indices) {
        result[i] = (a[i] xor b[i])
      }

      return result
    }

    private fun h(data: ByteArray): ByteArray {
      try {
        return MessageDigest.getInstance("SHA-1").digest(data)
      } catch (e: NoSuchAlgorithmException) {
        throw AuthorException("SHA-1 could not be found.", e)
      }

    }

    /**
     * 随机生成salt
     */
    private fun generate(length: Int): ByteArray {

      val RANDOM = SecureRandom()
      val salt = ByteArray(length)
      RANDOM.nextBytes(salt)
      return salt
    }

  }
}