package com.maxleap.bifrost.kotlin.api

import com.maxleap.bifrost.kotlin.core.BifrostConfig
import io.vertx.core.Future
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpClient
import io.vertx.core.json.Json
import org.apache.commons.codec.binary.StringUtils

/**
 * Created by.
 * User: ben
 * Date: 11/08/2017
 * Time: 3:23 PM
 * Email:benkris1@126.com
 * OpenTsDB 工具
 */
object OpenTsDBClient {
  private lateinit var httpClient: HttpClient

  fun init(httpClient: HttpClient) {

    this.httpClient = httpClient
  }

  fun putDataAsync(metric:String,timestamp:Long,value:Long,tags:Map<String,String>):Future<String>{
    val future = Future.future<String>()
    var buffer =   Buffer.buffer(Json.encode(mapOf("metric" to metric,"timestamp" to timestamp,"value" to value,"tags" to tags)))
    httpClient.post(BifrostConfig.openTsDBPort(),BifrostConfig.openTsDBHost(),"/api/put?details", {

        it.exceptionHandler {
          future.fail(it)
        }
        it.bodyHandler {
          future.complete(StringUtils.newStringUtf8(it.bytes))
        }
    })
      .putHeader("content-length", buffer.length().toString())
      .write(buffer)
      .end()
    return future
  }
}