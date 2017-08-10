package com.maxleap.bifrost.kotlin.core.model.op

import com.maxleap.bifrost.kotlin.core.model.MsgHeader
import com.maxleap.bifrost.kotlin.core.model.OpRequest
import com.maxleap.bifrost.kotlin.core.model.RequestType
import io.vertx.core.buffer.Buffer

/**
 * Created by.
 * User: ben
 * Date: 19/07/2017
 * Time: 9:48 AM
 * Email:benkris1@126.com
 *
 */
data class OpGetMore(val header: MsgHeader,
                     val zero: Int,
                     val collectionName:String,
                     val numberReturn:Int,
                     val cursorId: Long)
  :OpRequest(header,collectionName,RequestType.OP_GET_MORE) {


  companion object {

    fun fromBuffer(header: MsgHeader,buffer: Buffer): OpGetMore{
      val zero = buffer.getIntLE(12)
      val endPosition = endPosition(buffer, 16)
      val fullCollectionName = buffer.getString(16, endPosition)
      val numberReturn = buffer.getIntLE(endPosition + 1)
      val cursorId = buffer.getLongLE((endPosition + 5))
      return OpGetMore(header,zero, fullCollectionName, numberReturn, cursorId)
    }
  }


}
