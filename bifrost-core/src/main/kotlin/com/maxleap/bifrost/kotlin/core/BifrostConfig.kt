package com.maxleap.bifrost.kotlin.core

import io.vertx.core.json.JsonObject

/**
 * Created by.
 * User: ben
 * Date: 10/08/2017
 * Time: 5:33 PM
 * Email:benkris1@126.com
 *
 */
object BifrostConfig {

  private var port:Int = 0
  private var monitorEnable:Boolean = false
  private lateinit var openTsDBHost:String
  private var openTsDBPort:Int = 80

  fun init(config:JsonObject) {
    port = config.getInteger("port")
    this.monitorEnable =config.getJsonObject("monitor").getBoolean("enable")
    this.openTsDBHost = config.getJsonObject("monitor").getString("openTsDBHost")
    this.openTsDBPort = config.getJsonObject("monitor").getInteger("openTsDBPort")
  }

  fun port() = port
  fun monitorEnable() = monitorEnable
  fun openTsDBHost() =openTsDBHost
  fun openTsDBPort() = openTsDBPort
}