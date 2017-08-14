package com.maxleap.bifrost.kotlin

import com.maxleap.bifrost.kotlin.api.OpenTsDBClient
import com.maxleap.bifrost.kotlin.api.PandoraSupport
import com.maxleap.bifrost.kotlin.core.BifrostConfig
import com.maxleap.bifrost.kotlin.core.BifrostSwapper
import com.maxleap.bifrost.kotlin.core.ext.AsyncPool
import io.vertx.core.AbstractVerticle
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.net.NetClientOptions
import io.vertx.core.net.NetServer
import io.vertx.kotlin.core.net.NetServerOptions
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles

/**
 * Created by.
 * User: ben
 * Date: 14/07/2017
 * Time: 10:39 AM
 * Email:benkris1@gmail.com
 *
 */

class BifrostServer: AbstractVerticle() {
  private lateinit var bifrostServer:NetServer

  init {
    PandoraSupport
  }
  override fun start() {
    super.start()
    BifrostConfig.init(config())

    val options = HttpClientOptions()
    options.connectTimeout = 1000
    OpenTsDBClient.init(vertx.createHttpClient(options))

    bifrostServer = vertx.createNetServer(NetServerOptions(tcpNoDelay = true, usePooledBuffers = true))
    val netClient = vertx.createNetClient(NetClientOptions()
      .setReconnectAttempts(3)
      .setReconnectInterval(NetClientOptions.DEFAULT_RECONNECT_INTERVAL)
      .setConnectTimeout(1000)
    )

    this.bifrostServer.connectHandler {
      if (logger.isDebugEnabled) {
        logger.info("accept connection from ${it.remoteAddress()}")
      }
      it.pause()
      BifrostSwapper(it,netClient)
      it.resume()
    }
    AsyncPool.init(vertx)
    this.bifrostServer.listen(BifrostConfig.port(), {
      when (it.succeeded()) {
        true -> logger.info("\n  | |      ___    __ _    | '_ \\  / __|    | |     ___    _  _    __| |  \n" +
          "  | |__   / -_)  / _` |   | .__/ | (__     | |    / _ \\  | +| |  / _` |  \n" +
          "  |____|  \\___|  \\__,_|   |_|__   \\___|   _|_|_   \\___/   \\_,_|  \\__,_|  \n" +
          "_|\"\"\"\"\"|_|\"\"\"\"\"|_|\"\"\"\"\"|_|\"\"\"\"\"|_|\"\"\"\"\"|_|\"\"\"\"\"|_|\"\"\"\"\"|_|\"\"\"\"\"|_|\"\"\"\"\"| \n" +
          "\"`-0-0-'\"`-0-0-'\"`-0-0-'\"`-0-0-'\"`-0-0-'\"`-0-0-'\"`-0-0-'\"`-0-0-'\"`-0-0-' " +
          "\nbifrost server start at port:${BifrostConfig.port()} success! \n")
        else -> {
          logger.error("bifrost server start failed!!!", it.cause())
          System.exit(2)
        }
      }
    })

  }


  override fun stop() {
    super.stop()
    vertx.eventBus().close {
      if (it.succeeded()) {
        logger.info("The bifrost stop")
      } else {
        logger.error("The bifrost stop failed", it.cause())
      }
    }
  }

  companion object {
    private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
  }
}
