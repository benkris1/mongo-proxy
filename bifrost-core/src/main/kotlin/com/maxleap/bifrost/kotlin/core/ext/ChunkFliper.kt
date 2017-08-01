package com.maxleap.bifrost.kotlin.core.ext

import com.maxleap.bifrost.kotlin.core.model.*
import com.maxleap.bifrost.kotlin.core.model.op.*
import com.maxleap.bifrost.kotlin.core.utils.Callback
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.core.parsetools.RecordParser
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles

/**
 * Created by.
 * User: ben
 * Date: 17/07/2017
 * Time: 10:15 AM
 * Email:benkris1@126.com
 *
 */
class ChunkFliper(private val parse: RecordParser,
                  private val remoteAddress: String,
                  private val onHandshake: Callback<Handshake>,
                  private val onSASL: Callback<SASLModel>,
                  private val onChunk:Callback<Chunk>
) : Handler<Buffer> {
  private var state = ReadStat.CHUNK_HEADER_BSC
  private var length = 0
  override fun handle(buffer: Buffer) {
    when (state) {
      ReadStat.CHUNK_HEADER_BSC -> {
        length = buffer.getIntLE(0)
        this.state = ReadStat.HANDSHAKE
        this.parse.fixedSizeMode(length - 4)
      }
      ReadStat.HANDSHAKE -> {

        this.state = ReadStat.CHUNK_HEADER_BSC
        this.parse.fixedSizeMode(4)
        var msgHeader = MsgHeader.fromBuffer(length, buffer)
        when (msgHeader.type) {
          RequestType.OP_QUERY -> {
            val opQuery = OpQuery.fromBuffer(msgHeader, buffer)
            if (Handshake.isHandShake(opQuery)) {
              this.emit(Handshake(opQuery, remoteAddress))
            } else if (SASLModel.isSASL(opQuery)) {
              this.emit(SASLModel(opQuery))
            } else {
              this.emit(Chunk(opQuery,buffer))
            }
          }
          RequestType.OP_UPDATE -> {
            var opUpdate = OpUpdate.fromBuffer(msgHeader, buffer)
            this.emit(Chunk(opUpdate,buffer))
          }
          RequestType.OP_DELETE -> {
            val opDelete = OpDelete.fromBuffer(msgHeader, buffer)
            this.emit(Chunk(opDelete,buffer))
          }
          RequestType.OP_INSERT -> {
            val opInsert = OpInsert.fromBuffer(msgHeader, buffer)
            this.emit(Chunk(opInsert,buffer))
          }
          RequestType.OP_GET_MORE -> {
            val opGetMore = OpGetMore.fromBuffer(msgHeader, buffer)
            this.emit(Chunk(opGetMore,buffer))
          }
          RequestType.OP_KILL_CURSORS -> {
            val opKillCursors = OpKillCursors.fromBuffer(msgHeader, buffer)
            /**
             * TODO
             * Ignore
             */
          }
          else -> {
            /**
             * TODO
             */
            logger.error("un support operation:${msgHeader.type}")
            throw UnsupportedOperationException("un support operation:${msgHeader.type}")
          }

        }
      }
    }
  }

  private fun emit(handshake: Handshake) {
    this.onHandshake.invoke(handshake)
  }

  private fun emit(sasl: SASLModel) {
    this.onSASL.invoke(sasl)
  }

  private fun emit(chunk: Chunk) {
    this.onChunk.invoke(chunk)
  }
  private enum class ReadStat {
    CHUNK_HEADER_BSC,
    HANDSHAKE

  }

  companion object {
    private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

  }
}
