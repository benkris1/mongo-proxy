package com.maxleap.bifrost.kotlin.core.model

import com.maxleap.bifrost.kotlin.core.utils.Buffered
import com.maxleap.bifrost.kotlin.core.utils.Codecs
import io.vertx.core.buffer.Buffer
import org.bson.Document


/**
 * Created by.
 * User: ben
 * Date: 19/07/2017
 * Time: 11:09 AM
 * Email:benkris1@126.com
 *
 */
class Reply(val header: MsgHeader,
                     val responseFlags:Int = 0,
                     val cursorID:Long = 0L,
                     val startingFrom:Int = 0,
                     val documents:Array<Document> = arrayOf<Document>()):Buffered {
   private val buffer:Buffer = Buffer.buffer()

  /**
   * 构造reply header
   */
  private fun msgHeader(): Buffer {
    buffer.appendIntLE(0)
    buffer.appendIntLE(header.requestID)
    buffer.appendIntLE(header.requestID)
    buffer.appendIntLE(RequestType.OP_REPLY.code)
    return buffer
  }

  override fun toBuffer(): Buffer {
    msgHeader()
    buffer.appendIntLE(responseFlags)
    buffer.appendLongLE(cursorID)
    buffer.appendIntLE(startingFrom)
    buffer.appendIntLE(documents.size)
    buffer.appendBytes(Codecs.docsToByteArray(documents))
    buffer.setIntLE(0,buffer.length())
    return buffer
  }

  companion object {

     fun errorReply(header: MsgHeader, code:Int, msg:String):Reply {
       val document = Document().append("code", code).append("ok", false).append("\$err", msg).append("errmsg",msg)
       return Reply(header, 2, 0, 0, arrayOf(document))
     }
  }

}