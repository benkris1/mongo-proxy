package com.maxleap.bifrost.kotlin.core.model.admin.cmd

import com.maxleap.bifrost.kotlin.core.model.OpRequest
import com.maxleap.bifrost.kotlin.core.utils.Buffered
import io.vertx.core.buffer.Buffer

/**
 * Created by.
 * User: ben
 * Date: 01/08/2017
 * Time: 7:19 PM
 * Email:benkris1@126.com
 *https://docs.mongodb.com/manual/reference/method/db.currentOp/
 * db.currentOp({"ns":{'$regex':"^local\."}})
 */
data class CurrentOp(val opRequest: OpRequest):Buffered {
  override fun toBuffer(): Buffer {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

}