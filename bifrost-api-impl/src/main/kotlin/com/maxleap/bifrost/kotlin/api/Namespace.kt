package com.maxleap.bifrost.kotlin.api

import com.maxleap.bifrost.kotlin.core.model.NamespaceStatus
import com.mongodb.ServerAddress

/**
 * Created by.
 * User: ben
 * Date: 02/08/2017
 * Time: 3:49 PM
 * Email:benkris1@126.com
 *
 */
interface Namespace {

  fun getAddressAsString():String
  fun serveAddress():List<ServerAddress>
  fun namespaceStatus():NamespaceStatus
  fun onStatusChange(change:(status:NamespaceStatus) -> Unit)
  fun onClusterChange(change:(name:String) -> Unit)
}