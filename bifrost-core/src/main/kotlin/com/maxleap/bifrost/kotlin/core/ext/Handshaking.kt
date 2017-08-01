package com.maxleap.bifrost.kotlin.core.ext

import com.maxleap.bifrost.kotlin.core.Endpoint
import com.maxleap.bifrost.kotlin.core.model.Handshake
import com.maxleap.bifrost.kotlin.core.utils.Do
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles

/**
 * Created by.
 * User: ben
 * Date: 18/07/2017
 * Time: 6:51 PM
 * Email:benkris1@126.com
 *
 */
class Handshaking (private val endpoint: Endpoint,
                   val success: Do? = null,
                   val failed: Do? = null){

  fun validate(handshake: Handshake) {
    synchronized(this) {
      try {
      val cx = handshake.toModel()
      this.endpoint.write(cx.toBuffer())
        this.success?.invoke()
      }catch (e :Exception) {
        logger.error(e.message,e)
        this.failed?.invoke()
      }
    }
  }

  companion object {
    private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
  }
}