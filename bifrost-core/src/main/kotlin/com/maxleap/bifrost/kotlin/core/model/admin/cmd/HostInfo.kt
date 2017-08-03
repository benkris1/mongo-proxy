package com.maxleap.bifrost.kotlin.core.model.admin.cmd

import com.maxleap.bifrost.kotlin.core.model.OpRequest
import com.maxleap.bifrost.kotlin.core.model.Reply
import com.maxleap.bifrost.kotlin.core.utils.Buffered
import io.vertx.core.buffer.Buffer
import org.bson.BsonDateTime
import org.bson.Document

/**
 * Created by.
 * User: ben
 * Date: 01/08/2017
 * Time: 7:08 PM
 * Email:benkris1@126.com
 *
 */
data class HostInfo(val opRequest: OpRequest):Buffered {
  override fun toBuffer(): Buffer {
    val document = Document("ok", 1.0)
    val sysDoc = Document()
    sysDoc.put("currentTime",BsonDateTime(System.currentTimeMillis()))
    sysDoc.put("hostname","bifrost.leapcloud.cn")
    document.put("system",sysDoc)

    val os = Document()
    os.put("type",System.getProperty("os.name"))
    os.put("name","bifrost")
    os.put("version",System.getProperty("os.version"))
    os.put("arch",System.getProperty("os.arch"))
    os.put("java.version",System.getProperty("java.version"))
    document.put("os",os)
    val reply = Reply(opRequest.msgHeader,documents = arrayOf(document))
    return reply.toBuffer()
  }
}