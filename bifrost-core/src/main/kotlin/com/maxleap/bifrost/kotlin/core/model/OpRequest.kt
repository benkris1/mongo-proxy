package com.maxleap.bifrost.kotlin.core.model

import com.mongodb.MongoNamespace
import io.vertx.core.buffer.Buffer

/**
 * Created by.
 * User: ben
 * Date: 26/07/2017
 * Time: 12:06 PM
 * Email:benkris1@126.com
 *
 */
abstract class OpRequest(msgHeader: MsgHeader, fullCollectionName:String,val requestType:RequestType):OpBase(msgHeader) {

  val nameSpace:MongoNamespace

  init {
    nameSpace = MongoNamespace(fullCollectionName)
  }


  companion object {
    fun endPosition(buffer: Buffer, start: Int): Int {
      var start = start
      while (true) {
        if (buffer.getByte(++start).toInt() == 0) {
          break
        }
      }
      return start
    }
  }
}