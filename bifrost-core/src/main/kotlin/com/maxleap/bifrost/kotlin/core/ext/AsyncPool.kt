package com.maxleap.bifrost.kotlin.core.ext

import io.vertx.core.*

/**
 * Created by.
 * User: ben
 * Date: 07/08/2017
 * Time: 2:12 PM
 * Email:benkris1@126.com
 *
 */
object AsyncPool {
  private lateinit var vertx:Vertx

  fun init(vertx: Vertx) {
      this.vertx = vertx
  }

  fun <T> execute(handler: Handler<Future<T>>, event: Handler<AsyncResult<T>>) {
    vertx.executeBlocking(handler, false, event)
  }

  fun <T> execute(handler: Handler<Future<T>>): Future<T> {
    val future = Future.future<T>()
    vertx.executeBlocking(handler, false, future.completer())
    return future
  }

}