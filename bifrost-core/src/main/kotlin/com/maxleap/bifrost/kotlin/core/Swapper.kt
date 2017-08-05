package `as`.leap.raptor.core.impl


import com.maxleap.bifrost.kotlin.core.Endpoint
import com.maxleap.bifrost.kotlin.core.endpoint.DirectEndpoint
import com.maxleap.bifrost.kotlin.core.ext.Handshaking
import com.maxleap.bifrost.kotlin.core.ext.MgoTransport
import com.maxleap.bifrost.kotlin.core.ext.SASLAuth
import com.maxleap.bifrost.kotlin.core.model.Reply
import io.vertx.core.Vertx
import io.vertx.core.net.NetClient
import io.vertx.core.net.NetSocket
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.lang.invoke.MethodHandles
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * 数据交换区
 */
abstract class Swapper(
    socket: NetSocket,
    netClient: NetClient,
    reconnect: Int = 0
) : Closeable {
  private val isClosed = AtomicBoolean(false)
  private val endpoint: Endpoint

  init {
    endpoint = DirectEndpoint(socket)
    val handshaking = Handshaking(this.endpoint, failed = { this.close() })
    val saslAuth = SASLAuth(this.endpoint,failed = {this.close()})
    val mgoTransport = MgoTransport(endpoint,netClient,reconnect,failed = {this.close()})
    this.endpoint
      .onHandshake { handshaking.validate(it) }
      .onSASL { saslAuth.auth(it) }
      .onChunk { chunk ->
        if (saslAuth.pass()) {
          mgoTransport.transport(chunk)
        }else {
          this.endpoint.write(Reply.errorReply(chunk.opRequest.msgHeader,10086,"permission denied. must logon authentication").toBuffer())
          this.close()
        }
      }
      .onClose {
        this.close()
        mgoTransport.close()
      }
  }

  override fun close() {
    if (!this.isClosed.getAndSet(true)) {
      logger.info("***** swapper closed! *****")
      this.endpoint.close()
    }
  }


  companion object {
    private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
  }

}