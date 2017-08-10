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
data class OpQuery(val header: MsgHeader,
                    val flags:Int,
                    val collectionName:String,
                    val skip:Int,
                    val numberReturn:Int,
                    val query: Document)
  :OpRequest(header,collectionName,RequestType.OP_QUERY) {


  companion object {

    fun fromBuffer(header: MsgHeader,buffer: Buffer):OpQuery {
      val flags = buffer.getIntLE(12)
      val endPosition = endPosition(buffer, 16)
      val fullCollectionName = buffer.getString(16, endPosition)
      val numberToSkip = buffer.getIntLE(endPosition + 1)
      val numberToReturn = buffer.getIntLE(endPosition + 5)
      val bytes = buffer.getBytes(endPosition + 9, buffer.length())
      val document = Codecs.docFromByte(bytes)
      return OpQuery(header, flags, fullCollectionName, numberToSkip, numberToReturn, document)
    }
  }


}
