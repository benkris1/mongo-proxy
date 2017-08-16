package com.maxleap.bifrost.kotlin.core.impl

import com.maxleap.bifrost.kotlin.core.TransportListener
import com.maxleap.bifrost.kotlin.core.ext.JestSearchClient
import com.maxleap.bifrost.kotlin.core.model.MonitorTrace
import com.maxleap.bifrost.kotlin.core.model.OpRequest
import org.apache.commons.lang3.time.DateFormatUtils
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles

/**
 * Created by.
 * User: ben
 * Date: 15/08/2017
 * Time: 6:06 PM
 * Email:benkris1@126.com
 *
 */
class EsMonitorListener : TransportListener() {

  private lateinit var monitorTrace:MonitorTrace

  override fun transportStart(opRequest: OpRequest) {
    startTs = System.currentTimeMillis()

    this.monitorTrace = MonitorTrace.fromOpRequest(opRequest)
  }


  override fun transportEnd() {
    val timestamp = System.currentTimeMillis()
    monitorTrace.opTime = timestamp - startTs
    if(monitorTrace.opTime >= 10) { //只记录 大于10ms 请求

      JestSearchClient.put("$INDEX${DateFormatUtils.format(System.currentTimeMillis(),"YYYY-MM-dd")}",monitorTrace)
      .setHandler({
        if(it.succeeded()) {
        }else{
          logger.warn("send monitor trace to es error ${it.cause().message}",it.cause())
        }
      })
    }

  }

  override fun exception(throwable: Throwable) {
  }

  override fun close() {
  }

  companion object {
    private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
    private val INDEX = "bifrost-op-slow-"
  }

}