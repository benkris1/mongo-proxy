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
  private lateinit var openTsDB:String


  fun init(config:JsonObject) {
    port = config.getInteger("port")
    this.monitorEnable =config.getJsonObject("monitor").getBoolean("enable")
    this.openTsDB = config.getJsonObject("monitor").getString("openTsDB")
  }

  fun port() = port
  fun monitorEnable() = monitorEnable
  fun openTsDB() = openTsDB
}