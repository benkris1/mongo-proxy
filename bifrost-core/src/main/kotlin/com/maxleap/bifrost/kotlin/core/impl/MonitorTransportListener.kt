package com.maxleap.bifrost.kotlin.core.impl

import com.google.common.collect.Maps
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
    logger.info("transport start requestID  ${opRequest.msgHeader.requestID}")
  }

  override fun transportEnd() {
    monitorTrace.opTime = System.currentTimeMillis() - startTs
    logger.info("transport end responseTo ${monitorTrace}")

  }

  override fun exception(throwable: Throwable) {
  }

  companion object {
    private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
  }
}