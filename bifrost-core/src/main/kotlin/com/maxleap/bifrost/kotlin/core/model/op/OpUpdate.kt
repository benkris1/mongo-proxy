package com.maxleap.bifrost.kotlin.core.model.op

import com.maxleap.bifrost.kotlin.core.model.MsgHeader
import com.maxleap.bifrost.kotlin.core.model.OpRequest
import io.vertx.core.buffer.Buffer
import org.bson.Document

/**
 * Created by.
 * User: ben
 * Date: 19/07/2017
 * Time: 9:48 AM
 * Email:benkris1@126.com
 *
 */
data class OpUpdate(val header: MsgHeader,
                    val zero:Int = 0,
                    val collectionName:String,
                    val flags:Int,
                    val selector:Document ?,
                    var update:Document ?)
  :OpRequest(header,collectionName) {


  companion object {

    fun fromBuffer(header: MsgHeader,buffer: Buffer):OpUpdate {
      val zero = buffer.getIntLE(12)
      val endPosition = endPosition(buffer, 16)
      val fullCollectionName = buffer.getString(16, endPosition)
      val flag = buffer.getIntLE(endPosition + 1)
      return OpUpdate(header,zero, fullCollectionName,flag,null,null)
    }
  }


}
