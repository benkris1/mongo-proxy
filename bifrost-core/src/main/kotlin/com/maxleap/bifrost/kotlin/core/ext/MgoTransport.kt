package com.maxleap.bifrost.kotlin.core.ext

import com.maxleap.bifrost.kotlin.api.Namespace
import com.maxleap.bifrost.kotlin.api.NamespaceFactory
import com.maxleap.bifrost.kotlin.api.impl.PandoraNamespaceFactory
import com.maxleap.bifrost.kotlin.core.MgoWrapperException
import com.maxleap.bifrost.kotlin.core.endpoint.DirectEndpoint
import com.maxleap.bifrost.kotlin.core.model.*
import com.maxleap.bifrost.kotlin.core.model.admin.cmd.GetLog
import com.maxleap.bifrost.kotlin.core.model.admin.cmd.HostInfo
import com.maxleap.bifrost.kotlin.core.model.op.OpGetMore
import com.maxleap.bifrost.kotlin.core.model.op.OpQuery
import com.maxleap.bifrost.kotlin.core.utils.Buffered
import com.maxleap.bifrost.kotlin.core.utils.Callback
import com.maxleap.bifrost.kotlin.core.utils.Do
import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mongodb.ServerAddress
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.core.net.NetClient
import io.vertx.core.net.NetSocket
import org.apache.commons.lang3.RandomUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils
import org.bson.Document
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.lang.invoke.MethodHandles
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean


/**
 * Created by.
 * User: ben
 * Date: 25/07/2017
 * Time: 6:13 PM
 * Email:benkris1@126.com
 *
 */
class MgoTransport(val endpoint: DirectEndpoint,
                   val netClient: NetClient,
                   val failed: Do? = null):Closeable{

  private val isClosed = AtomicBoolean(false)
  private val netSocketWrapperFactory:NetSocketWrapperFactory

  init {
    netSocketWrapperFactory = NetSocketWrapperFactory()
  }

  fun transport(chunk:Chunk) {
    val opRequest = chunk.opRequest
    /**
     * TODO 需要校验那些admin 命令不支持
     *
     */
    try {
      filterCmd(opRequest)?.let { this.endpoint.write(it.toBuffer());return@transport}

      netSocketWrapperFactory.getSocketWrapper(opRequest.nameSpace.databaseName)?.let {
        swapper(opRequest,chunk.swapperBuffer(),it)
      }?:netSocketWrapperFactory.createSocketWrapper(opRequest.nameSpace.databaseName)
        .compose{
          it.connect()
        }
        .map{
          swapper(opRequest,chunk.swapperBuffer(),it)
        }
        .otherwise{
          logger.error(ExceptionUtils.getStackTrace(it))
          failResponse(opRequest, it.message ?: "can't connect mgo server for ${opRequest.nameSpace}")
        }
    }catch (throwable:Throwable){
      failResponse(opRequest,throwable.message?:"can't swapper mgo server for ${opRequest.nameSpace}")
    }

  }

  /**
   * 过滤admin cmd
   */
  private fun filterCmd(request: OpRequest):Buffered?{
    when(request.nameSpace.databaseName) {
        ADMIN_DB -> {
          if(request is OpQuery && request.nameSpace.collectionName.equals("\$cmd")) {
            when {
              request.query.containsKey(GET_LOG_CMD) -> {
                  return GetLog(request)
              }
              request.query.containsKey(HOST_INFO) -> {
                return HostInfo(opRequest = request)
              }
              else -> {
                throw MgoWrapperException("not support admin command ${request.nameSpace.collectionName}")
              }
            }
          }else {
            throw MgoWrapperException("not support admin command ${request.nameSpace.collectionName}")
          }
        }
        LOCAL_DB -> {
          throw MgoWrapperException("you have no permission to access the 'local' database ")
        }
    }
    return null
  }
  override fun close() {
    synchronized(this) {
      this.isClosed.set(true)
      this.netSocketWrapperFactory.close()
    }
  }

  private fun swapper(opRequest: OpRequest,buffer: Buffer,netSocketWrapper: NetSocketWrapper) {
    when(netSocketWrapper.dataSourceStatus) {
      NamespaceStatus.ENABLE -> {
        netSocketWrapper.write(buffer)
      }
      NamespaceStatus.READONLY -> {
        when(opRequest) {
          is OpQuery -> netSocketWrapper.write(buffer)
          is OpGetMore -> netSocketWrapper.write(buffer)
          else -> {
             //TODO 不需要返回
          }
        }
      }
    }
  }

  private fun failResponse(op: OpBase,msg:String): Unit {

    this.endpoint.write(Reply.errorReply(op.msgHeader,10086,msg).toBuffer())
  }


  inner class NetSocketWrapperFactory:Closeable {
    private val serverSockets = ConcurrentHashMap<String,NetSocketWrapper>()
    private val mgoNamespaceFactory :NamespaceFactory

    init {
      mgoNamespaceFactory = PandoraNamespaceFactory()
    }

    fun createSocketWrapper(collectionName: String):Future<NetSocketWrapper> {
      return AsyncPool.execute(Handler {
        var f= it
        var namespace = mgoNamespaceFactory.loadNamespace(collectionName)
        val urls = namespace?.getAddressAsString()
        val netSocketWrapper = serverSockets.values
          .filter { it.serverUrls.equals(urls) }
          .firstOrNull()

        netSocketWrapper?.let{
          serverSockets.putIfAbsent(collectionName, it)
          f.complete(it)
        }?:let {
          val serverAddress = findMaster(namespace.serveAddress())
          val netSocketWrapper_tmp =  NetSocketWrapper(collectionName,namespace,serverAddress,netClient,{this.onClose(it)})
          serverSockets.putIfAbsent(collectionName,netSocketWrapper_tmp)
          netSocketWrapper_tmp
          f.complete(netSocketWrapper_tmp)
        }
      })
    }

    fun getSocketWrapper(collectionName:String) : NetSocketWrapper? {
      val netSocketWrapper = serverSockets.get(collectionName)
      if(null != netSocketWrapper && null == netSocketWrapper.socket()) {
        serverSockets.remove(collectionName)
      }
      return netSocketWrapper
    }

    /**
     * doc https://docs.mongodb.com/manual/reference/replica-states/
     */
    private fun findMaster(serverAddress: List<ServerAddress>): ServerAddress {
      serverAddress.forEach {
        var mgoClient :MongoClient ?= null
        try{
          mgoClient = MongoClient(it,MongoClientOptions.builder().connectTimeout(10).build())
          val runCommand = mgoClient.getDatabase("admin").runCommand(Document("isMaster", 1))
          val primary = runCommand.getString("primary")
          if(null != primary) {
            val name = primary.split(":")
            return@findMaster ServerAddress(name[0],Integer.valueOf(name[1]))
          }else{
            val msg = runCommand.getString("msg")
            if(StringUtils.equals(msg,"isdbgrid")) {
              val pos =  RandomUtils.nextInt(0,serverAddress.size)
              return@findMaster serverAddress[pos]
            }
          }
        }catch (throwable :Throwable) {
          logger.error("can't get primary from ${serverAddress},error msg:${throwable.message}",throwable)
        }finally {
          mgoClient?.let {
            it.close()
          }
        }
      }
      throw MgoWrapperException("can't get primary from ${serverAddress}")
    }

    /**
     * NetSocketWrapper close 通知调用
     */
    fun onClose(netSocketWrapper: NetSocketWrapper) :Unit{
      if (netSocketWrapper != null) {
        serverSockets.forEach { k, v ->
          if (netSocketWrapper == v) {
            serverSockets.remove(k)
          }
        }
      }
    }


    override fun close() {
      logger.info("close  all mgo transport connection:${serverSockets.keys}")
      synchronized(this) {
        serverSockets.forEach({it.value.close()})
        serverSockets.clear()
      }
    }
  }

  inner class NetSocketWrapper(val collectionName:String, namespace: Namespace, val serverAddress: ServerAddress, val netClient: NetClient,val closed:Callback<NetSocketWrapper>?=null):Closeable {

    private var socket: NetSocket? = null
    val serverUrls:String
    var dataSourceStatus:NamespaceStatus
    init {
      serverUrls = namespace.getAddressAsString()
      /*val listenerId = "close_mongo_server_socket_kotlin_" + System.nanoTime()
      mgoDatabase.__cluster.addDataChangedListener(listenerId, { type, newValue ->
        if (type === PathAndValue.EventType.Changed) {
          *//**
           * TODO
           *//*
          mgoDatabase.get__cluster().clearDataChangedListener(listenerId)
        } else {
          logger.warn("[event ignore]type: " + type)
        }
      })*/
      dataSourceStatus = namespace.namespaceStatus()
      namespace.onChange { dataSourceStatus = it  }
    }

    fun connect(): Future<NetSocketWrapper>{
      var cf = Future.future<NetSocketWrapper>()
      netClient.connect(serverAddress.port,serverAddress.host, {
        if(it.succeeded()) {
          val socket = it.result()
          socket.exceptionHandler {
            if (logger.isDebugEnabled) {
              logger.debug("socket wrapper error.", it)
            }
            //failed?.invoke();
          }
          socket.closeHandler {
            if (logger.isDebugEnabled) {
              logger.debug("socket wrapper closed.")
            }
            this.socket = null
            closed?.invoke(this)
          }
          socket.handler{
            endpoint.write(it)
          }
          this.socket = socket
          cf.complete(this)
          if (logger.isDebugEnabled) {
            logger.debug("initialize socket wrapper success.")
          }
        }else {
          var cause = it.cause()
          logger.error("can't  connect nameSpace ${collectionName} cause:${cause.message}",cause)
          cf.fail(cause)
        }
      })
      return  cf
    }

    fun write(buffer:Buffer) {
      socket?.write(buffer) ?: throw MgoWrapperException("can't connect  mgo server for ${collectionName}.")
    }

    fun socket():NetSocket? = socket

    override fun close() {
      synchronized(this) {
        if (this.socket != null) {
          this.socket!!.close()
          this.socket = null
        }
      }
    }
  }

  companion object {
    private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
    private val ADMIN_DB = "admin"
    private val LOCAL_DB = "local"

    private val GET_LOG_CMD = "getLog"
    private val HOST_INFO = "hostInfo"
  }

}