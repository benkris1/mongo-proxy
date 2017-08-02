package com.maxleap.bifrost.kotlin.core.model.admin.cmd

import com.google.common.collect.Lists
import com.maxleap.bifrost.kotlin.core.model.OpBase
import com.maxleap.bifrost.kotlin.core.model.Reply
import com.maxleap.bifrost.kotlin.core.utils.Buffered
import io.vertx.core.buffer.Buffer
import org.bson.BsonArray
import org.bson.BsonDateTime
import org.bson.BsonString
import org.bson.Document

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
    val document = Document("ok", true)
    document.put("totalLinesWritten", 9)
    var logs = Lists.newArrayList<BsonString>()
    logs.add(BsonString("Welcome to join Leapcloud, I'm Bifrost and my job is access MongoDB. enjoy it."))
    logs.add(BsonString("If you have any problem or advice, please contact us. cma@maxleap.com"))
    val logo = " __       _______     ___      .______     ______  __        ______    __    __   _______  \n" +
      "|  |     |   ____|   /   \\     |   _  \\   /      ||  |      /  __  \\  |  |  |  | |       \\ \n" +
      "|  |     |  |__     /  ^  \\    |  |_)  | |  ,----'|  |     |  |  |  | |  |  |  | |  .--.  |\n" +
      "|  |     |   __|   /  /_\\  \\   |   ___/  |  |     |  |     |  |  |  | |  |  |  | |  |  |  |\n" +
      "|  `----.|  |____ /  _____  \\  |  |      |  `----.|  `----.|  `--'  | |  `--'  | |  '--'  |\n" +
      "|_______||_______/__/     \\__\\ | _|       \\______||_______| \\______/   \\______/  |_______/ \n" +
      "                                                                                           "

    logs.add(BsonString(logo))
    logs.add(BsonString("Author:Ben.Ma"))
    document.put("log", BsonArray(logs))
    val reply = Reply(op.msgHeader,documents = arrayOf(document))
    return reply.toBuffer()
  }

}