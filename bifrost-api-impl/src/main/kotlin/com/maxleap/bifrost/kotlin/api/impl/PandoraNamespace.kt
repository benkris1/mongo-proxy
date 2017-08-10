package com.maxleap.bifrost.kotlin.api.impl
import com.maxleap.bifrost.kotlin.api.Namespace
import com.maxleap.bifrost.kotlin.core.model.NamespaceStatus
import com.maxleap.pandora.config.mgo.MgoDatabase
import com.mongodb.ServerAddress
import com.maxleap.pandora.zk.PathAndValue
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles


/**
 * Created by.
 * User: ben
 * Date: 26/07/2017
 * Time: 9:56 AM
 * Email:benkris1@126.com
 *
 */
class PandoraNamespace(val mgoDatabase: MgoDatabase,var onStatusChange:(status:NamespaceStatus) -> Unit ? = {},var clusterChange:(name:String) ->Unit = {} ): Namespace {


  init {
    mgoDatabase.status.addDataChangedListener("kotlin_status_changed", {
      type, newValue ->
      onStatusChange?.invoke(NamespaceStatus.valueOf(mgoDatabase.status.valAsString))
    })

    val listenerId = "kotlin_close_mgo_server_socket_" + System.nanoTime()
    mgoDatabase.__cluster.addDataChangedListener(listenerId) { type, newValue ->
      if (type === PathAndValue.EventType.Changed) {
        clusterChange.invoke(mgoDatabase.name)
        mgoDatabase.__cluster.clearDataChangedListener(listenerId)
      } else {
        logger.warn("[event ignore]type: " + type)
      }
    }
  }

  override fun namespaceStatus(): NamespaceStatus {
     return NamespaceStatus.valueOf(mgoDatabase.status.valAsString)
  }


  override fun getAddressAsString() = mgoDatabase.mgoCluster.urlsAsString

  override fun serveAddress(): List<ServerAddress> {
     return mgoDatabase.mgoCluster.listServerAddress()
  }

  override fun onStatusChange(onChange: (onStatusChange: NamespaceStatus) -> Unit) {
     this.onStatusChange = onChange
  }

  override fun onClusterChange(change: (name: String) -> Unit) {
     this.clusterChange = change
  }

  companion object {
    private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
  }
}