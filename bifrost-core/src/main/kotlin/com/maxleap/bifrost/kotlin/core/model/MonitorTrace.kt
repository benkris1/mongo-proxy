package com.maxleap.bifrost.kotlin.core.model

import com.maxleap.bifrost.kotlin.core.model.op.OpDelete
import com.maxleap.bifrost.kotlin.core.model.op.OpGetMore
import com.maxleap.bifrost.kotlin.core.model.op.OpQuery
import com.maxleap.bifrost.kotlin.core.model.op.OpUpdate
import com.maxleap.bifrost.kotlin.core.utils.Codecs
import io.vertx.core.json.Json
import org.apache.commons.lang3.StringUtils
import org.bson.Document


/**
 * Created by.
 * User: ben
 * Date: 09/08/2017
 * Time: 3:31 PM
 * Email:benkris1@126.com
 * opTime 单位为纳秒
 */
data class MonitorTrace(val db: String, val collectionName: String, val op: TraceOp, var opTime: Long = 0L, val q: String) {


  companion object {

    fun fromOpRequest(opRequest: OpRequest): MonitorTrace {
      var op = TraceOp.R
      var q: String = StringUtils.EMPTY
      var collectionName = opRequest.nameSpace.collectionName
      when (opRequest) {
        is OpQuery -> {
          if (collectionName.equals("\$cmd")) {
            when {
              opRequest.query.containsKey("insert") -> {
                collectionName = opRequest.query.getString("insert")
                op = TraceOp.I
              }
              opRequest.query.containsKey("update") -> {
                collectionName = opRequest.query.getString("update")
                var updDocs: List<*>? = opRequest.query.get("updates",List::class.java)
                q = Json.mapper.writeValueAsString(updDocs)
                op = TraceOp.U
              }
              opRequest.query.containsKey("delete") -> {
                collectionName = opRequest.query.getString("delete")
                q = opRequest.query.get("deletes",Document::class.java).toJson()
                op = TraceOp.D
              }
              opRequest.query.containsKey("count") -> {
                collectionName = opRequest.query.getString("count")
                q = opRequest.query.get("query",Document::class.java).toJson()
                op = TraceOp.C
              }
              else -> {
                op = TraceOp.CMD
                q = opRequest.query.toJson()
              }
            }
          }else {
            q = opRequest.query.toJson()
            op = TraceOp.R
          }
        }
        is OpUpdate -> {
          q = opRequest.selector?.toJson() ?: StringUtils.EMPTY
          op = TraceOp.U
        }
        is OpGetMore -> {
          q = "get more op cursorId:${opRequest.cursorId},return:${opRequest.numberReturn}"
          op = TraceOp.R
        }
        is OpDelete -> {
          q = opRequest.selector.toJson()
          op = TraceOp.D
        }
        else -> {

        }
      }
      return MonitorTrace(opRequest.nameSpace.databaseName,
        collectionName,
        op,
        0L,
        q
      )
    }
  }

  override fun toString(): String {
    return "MonitorTrace(db='$db', collectionName='$collectionName', op=$op, opTime=${opTime}ms, q='$q')"
  }
}