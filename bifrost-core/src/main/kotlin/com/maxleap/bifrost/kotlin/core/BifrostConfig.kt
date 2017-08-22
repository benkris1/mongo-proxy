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

  private var tcpPort:Int = 27017
  private var httpPort:Int = 8500
  private var monitorEnable:Boolean = false
  private lateinit var openTsDBHost:String
  private var openTsDBPort:Int = 80
  private lateinit var es :String

  fun init(config:JsonObject) {
    tcpPort = config.getInteger("tcpPort")
    httpPort = config.getInteger("httpPort")
    this.monitorEnable =config.getJsonObject("monitor").getBoolean("enable")
    this.openTsDBHost = config.getJsonObject("monitor").getString("openTsDBHost")
    this.openTsDBPort = config.getJsonObject("monitor").getInteger("openTsDBPort")
    this.es = config.getJsonObject("monitor").getString("es")
  }

  fun tcpPort() = tcpPort
  fun httpPort() = httpPort
  fun monitorEnable() = monitorEnable
  fun openTsDBHost() =openTsDBHost
  fun openTsDBPort() = openTsDBPort
  fun es() = es
}