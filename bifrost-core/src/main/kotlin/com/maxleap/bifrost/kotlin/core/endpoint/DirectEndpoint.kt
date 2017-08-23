package com.maxleap.bifrost.kotlin.core.endpoint

import com.maxleap.bifrost.kotlin.core.Endpoint
import com.maxleap.bifrost.kotlin.core.ext.ChunkFliper
import io.vertx.core.buffer.Buffer
import io.vertx.core.net.NetSocket
import io.vertx.core.parsetools.RecordParser
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles

/**
 * Created by.
 * User: ben
 * Date: 14/07/2017
 * Time: 4:35 PM
 * Email:benkris1@126.com
 *
 */
class DirectEndpoint(private val socket: NetSocket): Endpoint() {
  private var closed = false

  init {
    val parser = RecordParser.newFixed(4, null)
    val fliper = ChunkFliper(parser,socket.remoteAddress().toString(), { this.onHandshake?.invoke(it) },{this.onSASL?.invoke(it)},{this.onChunk?.invoke(it)},{this.onError?.invoke(it)})
    parser.setOutput(fliper)
    this.socket.handler(parser)
    this.socket.closeHandler {
      this.onClose?.invoke(Unit)
      if (logger.isDebugEnabled) {
        logger.debug("endpoint closed.")
      }
    }
    this.socket.exceptionHandler {
      logger.error("endpoint socket error.", it)
      this.onError?.invoke(it)
    }
    if (logger.isDebugEnabled) {
      logger.debug("initialize endpoint success.")
    }
  }
  override fun write(buffer: Buffer): Endpoint {
    this.socket.write(buffer)
    return this
  }

  override fun close() {
    synchronized(this) {
      if (!closed) {
        this.socket.close()
        closed = true
      }
    }
  }
  companion object {
    private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
  }
}