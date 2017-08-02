package com.maxleap.bifrost.kotlin.api.impl
import com.maxleap.bifrost.kotlin.api.Namespace
import com.maxleap.bifrost.kotlin.core.model.NamespaceStatus
import com.maxleap.pandora.config.mgo.MgoDatabase
import com.mongodb.ServerAddress

/**
 * Created by.
 * User: ben
 * Date: 26/07/2017
 * Time: 9:56 AM
 * Email:benkris1@126.com
 *
 */
class PandoraNamespace(val mgoDatabase: MgoDatabase,var onChange:(status:NamespaceStatus) -> Unit ? = {}): Namespace {

  init {
    mgoDatabase.status.addDataChangedListener("kotlin_status_changed", {
      type, newValue ->
      onChange?.invoke(NamespaceStatus.valueOf(mgoDatabase.status.valAsString))
    })
  }

  override fun namespaceStatus(): NamespaceStatus {
     return NamespaceStatus.valueOf(mgoDatabase.status.valAsString)
  }


  override fun getAddressAsString() = mgoDatabase.mgoCluster.urlsAsString

  override fun serveAddress(): List<ServerAddress> {
     return mgoDatabase.mgoCluster.listServerAddress()
  }

  override fun onChange(onChange: (status: NamespaceStatus) -> Unit) {
     this.onChange = onChange
  }

}