package com.maxleap.bifrost.kotlin.core.model

import com.maxleap.bifrost.kotlin.core.model.op.OpQuery
import com.maxleap.bifrost.kotlin.core.utils.Buffered
import io.vertx.core.buffer.Buffer
import org.bson.*
import java.util.ArrayList

/**
 * Created by.
 * User: ben
 * Date: 18/07/2017
 * Time: 6:53 PM
 * Email:benkris1@126.com
 *
 */

data class Handshake(val op: OpBase,val remoteAddress:String){

  fun toModel():Buffered {
    if(op is OpQuery)  {
      return when{
         op.query.containsKey(IS_MASTER) || op.query.containsKey(IS_MASTER_L) -> IsMaster(header = op.header)
         op.query.containsKey(BUILD_INFO) -> BuildInfo(op.header)
         op.query.containsKey(GET_LAST_ERROR) -> LastError(op.header)
         op.query.containsKey(REPL_SET_GET_STATUS) -> ReplSetGetStatus(op.header)
         op.query.containsKey(WHAT_S_MY_URI) -> WhatSMyUri(op.header,remoteAddress)
         op.query.containsKey(GET_ONCE) -> GetOnce(op.header)
         else ->  throw UnsupportedOperationException("hand shake not support the query ${op.query}")
      }

    }else {
      throw UnsupportedOperationException("hand shake not support the ${op.msgHeader.type} request")
    }

  }

  /**
   * isMaster
   */
  class IsMaster(val header: MsgHeader):Buffered{
    override fun toBuffer(): Buffer {
      val document = Document("ok", true)
      document.put("setName", "Bifrost")
      document.put("setVersion", 75)
      //document.put("primary", "localhost:27017")
      //document.put("me", "localhost:27017")
      document.put("ismaster", true)
      document.put("secondary", false)
      document.put("localTime", BsonDateTime(System.currentTimeMillis()))
      document.put("maxWireVersion", 4)
      document.put("minWireVersion", 0)
      document.put("hosts", BsonArray(listOf(BsonString("localhost:27017"))))
      val reply = Reply(header,documents = arrayOf(document))
      return reply.toBuffer()
    }

  }

  /**
   * 版本信息
   */
  class BuildInfo(val header: MsgHeader):Buffered {
    override fun toBuffer(): Buffer {
      val document = Document("ok", true)
      val versionArray = ArrayList<Any>()
      versionArray.add(3)
      versionArray.add(0)
      versionArray.add(2)
      versionArray.add(0)
      document.put("versionArray", versionArray)
      val reply = Reply(header,documents = arrayOf(document))
      return reply.toBuffer()
    }
  }

  /**
   *
   */
  class LastError(val header: MsgHeader):Buffered {
    override fun toBuffer(): Buffer {
      val connectionId =  (System.currentTimeMillis() / 1000);
      val doc = Document("ok", 1.0)
      doc.put("connectionId", connectionId)
      doc.put("n", 0)
      doc.put("writtenTo", null)
      doc.put("err", null)
      doc.put("lastOp", BsonTimestamp(0, 0))
      val reply = Reply(header,documents = arrayOf(doc))
      return reply.toBuffer()
    }
  }

  /**
   * TODO
   */
  class ReplSetGetStatus(val header: MsgHeader):Buffered {
    override fun toBuffer(): Buffer {
      val doc = Document("ok", 1.0)
      doc.put("set", "Bifrost")
      doc.put("date",BsonDateTime(System.currentTimeMillis()))
      doc.put("myState",1)
      doc.put("term",-1)
      //doc.put("members", BsonArray(listOf(BsonString("localhost"))))
      val reply = Reply(header,documents = arrayOf(doc))
      return reply.toBuffer()
    }
  }

  class WhatSMyUri(val header: MsgHeader,val remoteAddress: String):Buffered {
    override fun toBuffer(): Buffer {
      val doc = Document("ok", 1.0)
      doc.put("you", remoteAddress)
      val reply = Reply(header,documents = arrayOf(doc))
      return reply.toBuffer()
    }
  }

  class GetOnce(val header: MsgHeader):Buffered {
    override fun toBuffer(): Buffer {
      val doc = Document("ok", 1.0)
      doc.put("nonce", "33630c506a873fc5")
      val reply = Reply(header,documents = arrayOf(doc))
      return reply.toBuffer()
    }
  }

  companion object {
    val IS_MASTER = "ismaster"
    val IS_MASTER_L = "isMaster"
    val BUILD_INFO = "buildinfo"
    val GET_LAST_ERROR = "getlasterror"
    val REPL_SET_GET_STATUS = "replSetGetStatus"
    val WHAT_S_MY_URI = "whatsmyuri"
    val GET_ONCE = "getnonce"


    fun isHandShake(op: OpBase): Boolean {
      if (op is OpQuery) {
        if (op.query.containsKey(IS_MASTER) || op.query.containsKey(IS_MASTER_L)
          || op.query.containsKey(BUILD_INFO)
          || op.query.containsKey(GET_LAST_ERROR)
          || op.query.containsKey(REPL_SET_GET_STATUS)
          || op.query.containsKey(WHAT_S_MY_URI)
          || op.query.containsKey(GET_ONCE)
          ) {
          return true
        }
        return false
      } else {
        return false
      }

    }
  }


}
