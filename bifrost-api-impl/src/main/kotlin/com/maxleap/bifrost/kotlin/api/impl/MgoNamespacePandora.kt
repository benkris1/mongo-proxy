package com.maxleap.bifrost.kotlin.api.impl
import com.maxleap.pandora.config.mgo.MgoDatabase
import com.maxleap.pandora.config.DatabaseVisitor
/**
 * Created by.
 * User: ben
 * Date: 26/07/2017
 * Time: 9:56 AM
 * Email:benkris1@126.com
 *
 */
class MgoNamespacePandora {

  private val mgoDatabaseVisitor: DatabaseVisitor<MgoDatabase>

  init {
    mgoDatabaseVisitor = PandoraSupport.mgoDatabaseVisitor
  }

  fun getMgoDatabase(db: String): MgoDatabase {
      return mgoDatabaseVisitor.get(db)
  }


}