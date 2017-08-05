package `com`.maxleap.bifrost.kotlin.core.utils
import com.google.common.base.Splitter
import com.google.common.hash.Hashing
import com.maxleap.bifrost.kotlin.core.model.Reply
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.lang3.StringUtils
import org.bson.*
import org.bson.codecs.DecoderContext
import org.bson.codecs.DocumentCodec
import org.bson.codecs.EncoderContext
import org.bson.io.BasicOutputBuffer
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles
import java.nio.ByteBuffer
import java.util.regex.Pattern

object Codecs {

  private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
  private val DOCUMENT_CODEC = DocumentCodec()
  private val HEADROOM = 16 * 1024
  private val MAX_DOCUMENT_SIZE = 1024 * 1024 * 4


  private val SPACE_PATTERN by lazy {
    Pattern.compile("\\s+")
  }

  fun md5(bytes: ByteArray): String {
    return DigestUtils.md5Hex(bytes)
  }

  fun decodeHex(str: String, removeSpace: Boolean = false): ByteArray {
    if (!removeSpace) {
      return Hex.decodeHex(str.toCharArray())
    }
    val sb = StringBuffer(str.length)
    Splitter.on(SPACE_PATTERN).split(str).forEach {
      sb.append(it)
    }
    return decodeHex(sb.toString())
  }

  fun encodeHex(bytes: ByteArray, pretty: Boolean = false): String {
    if (bytes.isEmpty()) {
      return StringUtils.EMPTY
    }
    if (!pretty) {
      return Hex.encodeHexString(bytes)
    }
    val arr = Hex.encodeHexString(bytes).toCharArray()
    val sb = StringBuffer()
    for (i in 1 until arr.size) {
      sb.append(arr[i - 1])
      when {
        i and 31 == 0 -> sb.append(StringUtils.LF)
        i and 15 == 0 -> sb.append(StringUtils.SPACE).append(StringUtils.SPACE)
        i and 1 == 0 -> sb.append(StringUtils.SPACE)
      }
    }
    sb.append(arr.last())
    return sb.toString()
  }

  fun encodeBase64(bytes: ByteArray): String {
    return Base64.encodeBase64String(bytes)
  }

  fun decodeBase64(str: String): ByteArray {
    return Base64.decodeBase64(str)
  }

  fun murmur128(bytes: ByteArray): String {
    return Hashing.murmur3_128().newHasher()
        .putBytes(bytes)
        .hash()
        .toString()
  }

  fun murmur32(bytes: ByteArray): String {
    return Hashing.murmur3_32().newHasher()
        .putBytes(bytes)
        .hash()
        .toString()
  }


  fun docsToByteArray(documents: Array<Document>): ByteArray? {
    val outputBuffer = BasicOutputBuffer()
    val writer = BsonBinaryWriter(BsonWriterSettings(),
      BsonBinaryWriterSettings(MAX_DOCUMENT_SIZE + HEADROOM),outputBuffer)
    try{
      documents.forEach {
        DOCUMENT_CODEC.encode(writer, it, EncoderContext.builder().isEncodingCollectibleDocument(true).build())
      }
      return outputBuffer.toByteArray()
    }finally {
      writer.close()
      outputBuffer.close()
    }
  }

  fun docFromByte(bytes:ByteArray): Document {
    BsonBinaryReader(ByteBuffer.wrap(bytes)).use {
      return DOCUMENT_CODEC.decode(it, DecoderContext.builder().build())
    }
  }
}