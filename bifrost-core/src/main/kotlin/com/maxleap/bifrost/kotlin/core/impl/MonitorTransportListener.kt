package com.maxleap.bifrost.kotlin.core.impl

import com.maxleap.bifrost.kotlin.api.OpenTsDBClient
import com.maxleap.bifrost.kotlin.core.TransportListener
import com.maxleap.bifrost.kotlin.core.model.MonitorTrace
import com.maxleap.bifrost.kotlin.core.model.OpRequest
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles

/**
 * Created by.
 * User: ben
 * Date: 09/08/2017
 * Time: 2:29 PM
 * Email:benkris1@126.com
 *把请求指标 发到openTsDb打点 方便监控性能指标
 */

class MonitorTransportListener: TransportListener() {
  private lateinit var monitorTrace:MonitorTrace
  private var startTs = 0L
  override fun close() {
  }

  override fun transportStart(opRequest: OpRequest) {
    startTs = System.currentTimeMillis()
    this.monitorTrace = MonitorTrace.fromOpRequest(opRequest)
  }

  override fun transportEnd() {
    val timestamp = System.currentTimeMillis()
    monitorTrace.opTime = timestamp - startTs
    if(monitorTrace.opTime >= 10) {

      OpenTsDBClient.putDataAsync(
        METRICS,
        Math.floorDiv(timestamp,1000),
        monitorTrace.opTime,
        mapOf("db" to monitorTrace.db,
          "collection" to monitorTrace.collectionName,
          "op" to monitorTrace.op.name
        )
      ).setHandler({
        if(it.succeeded()) {
          logger.info(it.result())
        }else{
          logger.warn("send monitor trace to opTsDB error ${it.cause().message}",it.cause())
        }
      })
    }

  }

  override fun exception(throwable: Throwable) {
  }

  companion object {
    private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
    private val METRICS = "bifrost.op.ms.slow.value"
  }
}