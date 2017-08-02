package com.maxleap.bifrost.kotlin.api.impl

import com.maxleap.bifrost.kotlin.api.Namespace
import com.maxleap.bifrost.kotlin.api.NamespaceFactory
import com.maxleap.bifrost.kotlin.api.PandoraSupport
import com.maxleap.pandora.config.DatabaseVisitor
import com.maxleap.pandora.config.mgo.MgoDatabase

/**
 * Created by.
 * User: ben
 * Date: 02/08/2017
 * Time: 4:19 PM
 * Email:benkris1@126.com
 *
 */
class PandoraNamespaceFactory:NamespaceFactory {
   private val mgoDatabaseVisitor: DatabaseVisitor<MgoDatabase>

  init {
    mgoDatabaseVisitor = PandoraSupport.mgoDatabaseVisitor
  }

  override fun loadNamespace(db: String): Namespace {
    val mgoDatabase = mgoDatabaseVisitor.get(db)
    if(null == mgoDatabase) {
      throw IllegalStateException("can't get ${db} mgo address from pandora.")
    }
    return PandoraNamespace(mgoDatabase)
  }

}