package com.maxleap.bifrost.kotlin.core.model.op

import com.google.common.collect.Lists
import com.maxleap.bifrost.kotlin.core.model.MsgHeader
import com.maxleap.bifrost.kotlin.core.model.OpBase
import io.vertx.core.buffer.Buffer

/**
 * Created by.
 * User: ben
 * Date: 19/07/2017
 * Time: 9:48 AM
 * Email:benkris1@126.com
 *
 */
data class OpKillCursors(val header: MsgHeader,
                    val zero:Int,
                    val numberOfCursorIDs:Int,
                         val cursorIds:List<Long>
                    )
  :OpBase(header) {


  companion object {

    fun fromBuffer(header: MsgHeader,buffer: Buffer):OpKillCursors {
      val zero = buffer.getIntLE(12)
      val numberOfCursorIDs = buffer.getIntLE(16)
      val cursorIds = Lists.newArrayList<Long>()
      for(i in 0 until  numberOfCursorIDs) {
         cursorIds.add(buffer.getLongLE(20+8*i))
      }
      return OpKillCursors(header,zero,numberOfCursorIDs,cursorIds )
    }
  }


}
