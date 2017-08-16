package com.maxleap.bifrost.kotlin.core.ext

import com.maxleap.bifrost.kotlin.core.BifrostConfig
import com.maxleap.bifrost.kotlin.core.model.MonitorTrace
import io.searchbox.client.JestClient
import io.searchbox.client.JestClientFactory
import io.searchbox.client.JestResultHandler
import io.searchbox.client.config.HttpClientConfig
import io.searchbox.core.DocumentResult
import io.searchbox.core.Index
import io.vertx.core.Future
import org.apache.commons.lang3.time.DateFormatUtils
import java.lang.Exception

/**
 * Created by.
 * User: ben
 * Date: 15/08/2017
 * Time: 6:22 PM
 * Email:benkris1@126.com
 *
 */
object JestSearchClient {

  private val jestClient: JestClient?

  init {
    val factory = JestClientFactory()
    factory.setHttpClientConfig(HttpClientConfig.Builder(BifrostConfig.es())
      .multiThreaded(true)
      .maxTotalConnection(100)
      .build())
    this.jestClient = factory.`object`
  }


  fun put(index:String,monitorTrace: MonitorTrace):Future<String> {
    val future = Future.future<String>()
    val index = Index.Builder(mapOf("op" to monitorTrace.op.name,
      "@timestamp" to DateFormatUtils.ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT.format(System.currentTimeMillis()),
      "collection" to monitorTrace.collectionName,
      "db" to monitorTrace.db,
      "opTime" to monitorTrace.opTime,
      "q" to monitorTrace.q)
    ).index(index).type("bifrost").build()

    jestClient?.executeAsync(index,object : JestResultHandler<DocumentResult> {
      override fun completed(result: DocumentResult?) {
        future.complete(result.toString())
      }

      override fun failed(ex: Exception?) {
        future.fail(ex)
      }
    })

    return future
  }

}