package com.maxleap.bifrost.kotlin.api.impl
import com.maxleap.bifrost.kotlin.api.Namespace
import com.maxleap.bifrost.kotlin.core.model.NamespaceStatus
import com.mongodb.ServerAddress
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
class DefaultNamespace(var onStatusChange:(status:NamespaceStatus) -> Unit ? = {}, var clusterChange:(name:String) ->Unit = {} ): Namespace {


  /**
   * TODO implement by yourself
   */
  override fun namespaceStatus(): NamespaceStatus {
     return NamespaceStatus.ENABLE
  }


  /**
   * TODO
   * implement by yourself
   */
  override fun getAddressAsString() = "mongo.userdata.2:27013"

  /**
   * TODO
   * implement by yourself
   */
  override fun serveAddress(): List<ServerAddress> {
     return  listOf(ServerAddress("mongo.userdata.2",27013))
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