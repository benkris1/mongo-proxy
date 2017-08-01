package com.maxleap.bifrost.kotlin.core.model

import com.maxleap.bifrost.kotlin.core.model.op.OpQuery
import org.bson.types.Binary

/**
 * Created by.
 * User: ben
 * Date: 21/07/2017
 * Time: 7:22 PM
 * Email:benkris1@126.com
 *
 */
data class SASLModel(val op: OpBase) {

  fun toModel():Any {
    if(op is OpQuery)  {
      return when{
        op.query.containsKey(SASL_START) -> {
            SASLStart(op.query.getString(MECHANISM),(op.query.get("payload") as Binary).data)
        }
        op.query.containsKey(SASL_CONTINUE) -> {
            SASLContinue(op.query.getInteger("conversationId"),(op.query.get("payload") as Binary).data)
        }
        else ->  throw UnsupportedOperationException("SASL not support the query ${op.query}")
      }

    }else {
      throw UnsupportedOperationException("SASL not support the ${op.msgHeader.type} request")
    }

  }

  class SASLStart(val mechanism:String,val payload:ByteArray) {

  }

  class SASLContinue(val conversationId:Int,val payload: ByteArray) {

  }

  companion object {
    val SASL_START = "saslStart"
    val SASL_CONTINUE = "saslContinue"
    val MECHANISM = "mechanism"
    fun isSASL(op: OpBase):Boolean {
      if (op is OpQuery) {
        if (op.query.containsKey(SASL_CONTINUE) || op.query.containsKey(SASL_START)) {
          return true
        }
        return false
      } else {
        return false
      }
    }
  }


}