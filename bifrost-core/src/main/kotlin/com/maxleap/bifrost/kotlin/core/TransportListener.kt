package com.maxleap.bifrost.kotlin.core

import com.maxleap.bifrost.kotlin.core.model.OpRequest
import io.vertx.core.buffer.Buffer
import java.io.Serializable

/**
 * Created by.
 * User: ben
 * Date: 09/08/2017
 * Time: 2:22 PM
 * Email:benkris1@126.com
 *
 */
abstract  class TransportListener :Serializable {
  private var maxLength  = -1
  private var currentLength = -1
  /**
   * 数据转发开始
   */
  abstract fun  transportStart(opRequest: OpRequest)

  /**
   *数据转发结束
   */
 abstract fun transportEnd()

  /**
   * 异常处理
   */
 abstract fun exception(throwable: Throwable)

 /**
  * 关闭
  */
 abstract fun close()


 fun transport(buffer:Buffer) {
  if(maxLength == -1) {
   maxLength = buffer.getIntLE(0)
   if(maxLength == buffer.length()) {
    transportEnd()
    maxLength = -1
   }else {
     currentLength = maxLength - buffer.length()
   }
  }else {
    currentLength = currentLength - buffer.length()
    if(currentLength <=0) {
     transportEnd()
     maxLength = -1
     currentLength = -1
    }
  }

 }
}