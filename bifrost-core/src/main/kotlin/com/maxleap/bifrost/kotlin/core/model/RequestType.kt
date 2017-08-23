package com.maxleap.bifrost.kotlin.core.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

/**
 * Created by.
 * User: ben
 * Date: 18/07/2017
 * Time: 10:55 AM
 * Email:benkris1@126.com
 *
 */
enum class RequestType(val code: Int) {
  OP_REPLY(1),
  OP_MSG(1000),
  OP_UPDATE(2001),
  OP_INSERT(2002),
  RESERVED(2003),
  OP_QUERY(2004),
  OP_GET_MORE(2005),
  OP_DELETE(2006),
  OP_KILL_CURSORS(2007),
  OP_COMMAND(2010);
  companion object {

    @JsonCreator
    fun toRequestType(opCode:Int):RequestType {
      for (requestType in RequestType.values()) {
        if (requestType.code == opCode) {
          return requestType
        }
      }
        throw IllegalArgumentException("Not valid request type: $opCode.")
    }
  }



}