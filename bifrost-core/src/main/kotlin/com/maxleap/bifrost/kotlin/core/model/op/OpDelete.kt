package com.maxleap.bifrost.kotlin.core.model.op

import com.maxleap.bifrost.kotlin.core.model.MsgHeader
import com.maxleap.bifrost.kotlin.core.model.OpRequest
import com.maxleap.bifrost.kotlin.core.model.RequestType
import com.maxleap.bifrost.kotlin.core.utils.Codecs
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
data class OpDelete(val header: MsgHeader,
                    val zero:Int,
                    val collectionName:String,
                    val flags:Int,
                    val selector: Document)
  :OpRequest(header,collectionName,RequestType.OP_DELETE) {


  companion object {

    fun fromBuffer(header: MsgHeader,buffer: Buffer):OpDelete {
      val zero = buffer.getIntLE(12)
      val endPosition = endPosition(buffer, 16)
      val fullCollectionName = buffer.getString(16, endPosition)
      val flags = buffer.getIntLE(endPosition + 1)
      val bytes = buffer.getBytes(endPosition + 5, buffer.length())
      val document = Codecs.docFromByte(bytes)
      return OpDelete(header,zero, fullCollectionName, flags, document)
    }
  }


}
