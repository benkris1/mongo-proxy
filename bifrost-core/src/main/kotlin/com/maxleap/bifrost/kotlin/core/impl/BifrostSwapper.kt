package com.maxleap.bifrost.kotlin.core

import com.maxleap.bifrost.kotlin.Swapper
import io.vertx.core.net.NetClient
import io.vertx.core.net.NetSocket


/**
 * Created by.
 * User: ben
 * Date: 14/07/2017
 * Time: 3:46 PM
 * Email:benkris1@126.com
 *
 */

class BifrostSwapper(socket: NetSocket,
                     netClient: NetClient)
  : Swapper(socket,netClient) {


}
