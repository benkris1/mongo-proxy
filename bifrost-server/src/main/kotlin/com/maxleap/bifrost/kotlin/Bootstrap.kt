package com.maxleap.bifrost.kotlin

import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject

/**
 * Created by.
 * User: ben
 * Date: 22/08/2017
 * Time: 6:27 PM
 * Email:benkris1@126.com
 *
 */
object Bootstrap {

  private val config: JsonObject? by lazy {
    Thread.currentThread().contextClassLoader.getResourceAsStream("config.json").use {
       val config = JsonObject(Json.mapper.readValue(it,Map::class.java) as Map<String,Any>)
       config
    }
  }

  @JvmStatic
  fun main(args: Array<String>) {
    val vertx = Vertx.vertx()
    val options = DeploymentOptions()

    options.config = config
    vertx.deployVerticle(BifrostServer(), options) { event ->
      if (event.succeeded()) {

      } else {

      }
    }

  }
}