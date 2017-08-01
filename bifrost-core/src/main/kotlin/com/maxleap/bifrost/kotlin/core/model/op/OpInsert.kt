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
data class OpInsert(val header: MsgHeader,
                    val flags:Int,
                    val collectionName:String,
                    val insert: Array<Document>)
  :OpRequest(header,collectionName) {


  companion object {

    fun fromBuffer(header: MsgHeader,buffer: Buffer):OpInsert {
      val flags = buffer.getIntLE(12)
      val endPosition = endPosition(buffer, 16)
      val fullCollectionName = buffer.getString(16, endPosition)
      return OpInsert(header, flags, fullCollectionName, arrayOf() )
    }
  }


}
