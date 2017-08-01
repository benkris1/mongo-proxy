package com.maxleap.bifrost.kotlin.core.model.admin.cmd

import com.maxleap.bifrost.kotlin.core.model.OpBase
import com.maxleap.bifrost.kotlin.core.utils.Buffered
import io.vertx.core.buffer.Buffer

/**
 * Created by.
 * User: ben
 * Date: 28/07/2017
 * Time: 4:11 PM
 * Email:benkris1@126.com
 *
 */
data class GetLog(val op:OpBase):Buffered {

  override fun toBuffer(): Buffer {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

}