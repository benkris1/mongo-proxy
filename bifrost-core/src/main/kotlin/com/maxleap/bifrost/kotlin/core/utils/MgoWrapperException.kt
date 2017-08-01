package com.maxleap.bifrost.kotlin.core

/**
 * Created by.
 * User: ben
 * Date: 24/07/2017
 * Time: 8:16 PM
 * Email:benkris1@126.com
 *
 */
class MgoWrapperException(msg:String) : RuntimeException(msg) {

  constructor(msg:String,e:Throwable):this(msg) {
    /**
     * TODO
     */
  }
}