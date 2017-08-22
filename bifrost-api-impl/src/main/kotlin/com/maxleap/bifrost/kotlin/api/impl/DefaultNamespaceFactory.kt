package com.maxleap.bifrost.kotlin.api.impl

import com.maxleap.bifrost.kotlin.api.Namespace
import com.maxleap.bifrost.kotlin.api.NamespaceFactory

/**
 * Created by.
 * User: ben
 * Date: 02/08/2017
 * Time: 4:19 PM
 * Email:benkris1@126.com
 *
 * implement by yourself
 * you can store db -> host in same database.like mysql,zk
 * this is demo.
 */
class DefaultNamespaceFactory :NamespaceFactory {


  /**
   * TODO
   */
  override fun loadNamespace(db: String): Namespace {

    return DefaultNamespace()
  }

}