package com.maxleap.bifrost.kotlin.core.model

import com.google.common.base.MoreObjects
import com.maxleap.bifrost.kotlin.core.utils.Buffered
import io.vertx.core.buffer.Buffer

/**
 * Created by.
 * User: ben
 * Date: 18/07/2017
 * Time: 10:52 AM
 * Email:benkris1@126.com
 *
 */
data class MsgHeader(val length:Int,
                     val requestID:Int,
                      val responseTo:Int,
                     val type:RequestType
                     ):Buffered {

  override fun toBuffer(): Buffer {
    val buffer = Buffer.buffer()
      .appendInt(length)
      .appendInt(requestID)
      .appendInt(responseTo)
      .appendInt(type.code)

    return buffer
  }

  override fun toString(): String {
    return MoreObjects.toStringHelper(this)
      .add("length", this.length)
      .add("requestID", this.requestID)
      .add("responseTo", this.responseTo)
      .add("type", this.type)
      .omitNullValues()
      .toString()
  }

  companion object {

    fun fromBuffer(length:Int,buffer: Buffer):MsgHeader {
      val requestID = buffer.getIntLE(0)
      val responseTo = buffer.getIntLE(4)
      val opCode = buffer.getIntLE(8)
      val requestType = RequestType.toRequestType(opCode)
      return MsgHeader(length, requestID, responseTo, requestType)
    }
  }

}