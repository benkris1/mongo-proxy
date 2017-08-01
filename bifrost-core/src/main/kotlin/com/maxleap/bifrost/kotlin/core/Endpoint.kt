package com.maxleap.bifrost.kotlin.core

import com.google.common.base.Preconditions
import com.maxleap.bifrost.kotlin.core.model.Chunk
import com.maxleap.bifrost.kotlin.core.model.Handshake
import com.maxleap.bifrost.kotlin.core.model.SASLModel
import com.maxleap.bifrost.kotlin.core.utils.Callback
import io.vertx.core.buffer.Buffer
import java.io.Closeable

/**
 * Created by.
 * User: ben
 * Date: 14/07/2017
 * Time: 4:36 PM
 * Email:benkris1@126.com
 *
 */
abstract  class Endpoint:Closeable {
  protected var onHandshake: Callback<Handshake>? = null
  protected var onChunk: Callback<Chunk>? = null
  protected var onError: Callback<Throwable>? = null
  protected var onClose: Callback<Unit>? = null
  protected var onSASL: Callback<SASLModel>? = null


  fun onChunk(consumer: Callback<Chunk>): Endpoint {
    synchronized(this) {
      Preconditions.checkArgument(this.onChunk == null, "chunk handler exists already!")
      this.onChunk = consumer
      return this
    }
  }


  fun onHandshake(consumer: Callback<Handshake>): Endpoint {
    synchronized(this) {
      Preconditions.checkArgument(this.onHandshake == null, "handshake handler exists already!")
      this.onHandshake = consumer
      return this
    }
  }

  fun onSASL(consumer: Callback<SASLModel>): Endpoint {
    synchronized(this) {
      Preconditions.checkArgument(this.onSASL == null, "SASL handler exists already!")
      this.onSASL = consumer
      return this
    }
  }

  fun onClose(handler: Callback<Unit>): Endpoint {
    synchronized(this) {
      Preconditions.checkArgument(this.onClose == null, "close handler exists already!")
      this.onClose = handler
      return this
    }
  }

  fun onError(handler: Callback<Throwable>): Endpoint {
    synchronized(this) {
      Preconditions.checkArgument(this.onError == null, "error handler exists already!")
      this.onError = handler
      return this
    }
  }

  abstract fun write(buffer: Buffer): Endpoint
}