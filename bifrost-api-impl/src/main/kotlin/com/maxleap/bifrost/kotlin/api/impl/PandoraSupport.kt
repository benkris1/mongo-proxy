package com.maxleap.bifrost.kotlin.api.impl

import com.google.inject.Guice
import com.google.inject.Injector
import com.maxleap.pandora.config.ConfigModule
import com.maxleap.pandora.config.DatabaseVisitor
import com.maxleap.pandora.config.mgo.MgoDatabase


/**
 * Created by.
 * User: ben
 * Date: 26/07/2017
 * Time: 10:51 AM
 * Email:benkris1@126.com
 *
 */
object  PandoraSupport {

   val injector:Injector
   val mgoDatabaseVisitor: DatabaseVisitor<MgoDatabase>
  init {

    injector = Guice.createInjector(ConfigModule())
    mgoDatabaseVisitor = injector.getInstance(DatabaseVisitor::class.java) as DatabaseVisitor<MgoDatabase>
  }

  fun <T> getInstance(type:Class<T>): T = injector.getInstance(type)

}