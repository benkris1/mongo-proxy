package com.maxleap.bifrost.kotlin.core.model

import com.maxleap.bifrost.kotlin.core.utils.SwapperBuffered
import io.vertx.core.buffer.Buffer

/**
 * Created by.
 * User: ben
 * Date: 25/07/2017
 * Time: 6:31 PM
 * Email:benkris1@126.com
 *
 */
data class Chunk(val opRequest: OpRequest,val payLoad:Buffer) :SwapperBuffered {

  /**
   * 完整的swapper流
   */
  override fun swapperBuffer(): Buffer {
     return Buffer.buffer().appendIntLE(opRequest.msgHeader.length).appendBuffer(payLoad)
  }

}