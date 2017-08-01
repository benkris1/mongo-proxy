package com.maxleap.bifrost.kotlin.core.ext

import com.google.common.base.Objects
import com.maxleap.bifrost.kotlin.api.impl.MgoNamespacePandora
import com.maxleap.bifrost.kotlin.core.MgoWrapperException
import com.maxleap.bifrost.kotlin.core.endpoint.DirectEndpoint
import com.maxleap.bifrost.kotlin.core.model.Chunk
import com.maxleap.bifrost.kotlin.core.model.OpBase
import com.maxleap.bifrost.kotlin.core.model.OpRequest
import com.maxleap.bifrost.kotlin.core.model.Reply
import com.maxleap.bifrost.kotlin.core.model.op.OpGetMore
import com.maxleap.bifrost.kotlin.core.model.op.OpQuery
import com.maxleap.bifrost.kotlin.core.utils.Callback
import com.maxleap.bifrost.kotlin.core.utils.Do
import com.maxleap.pandora.config.DataSourceStatus
import com.maxleap.pandora.config.HostAndPort
import com.maxleap.pandora.config.mgo.MgoCluster
import com.maxleap.pandora.config.mgo.MgoDatabase
import com.mongodb.MongoClient
import com.mongodb.MongoClientException
import com.mongodb.MongoClientOptions
import com.mongodb.ServerAddress
import io.vertx.core.buffer.Buffer
import io.vertx.core.net.NetClient
import io.vertx.core.net.NetSocket
import org.apache.commons.collections4.CollectionUtils
import org.bson.Document
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.lang.invoke.MethodHandles
import java.util.concurrent.CompletableFuture
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
                   val reconnect:Int=0,
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
      netSocketWrapperFactory.getSocketWrapper(opRequest.nameSpace.databaseName)?.let {
        swapper(opRequest,chunk.swapperBuffer(),it)
      }?:netSocketWrapperFactory.createSocketWrapper(opRequest.nameSpace.databaseName)
        .thenComposeAsync {
          it.connect()
        }
        .thenApply {
          swapper(opRequest,chunk.swapperBuffer(),it)
        }
        .exceptionally {
          logger.info(it.message,it)
          failResponse(opRequest,it.message?:"can't connect mgo server for ${opRequest.nameSpace}")
          null
        }
    }catch (throwable:Throwable){
      failResponse(opRequest,throwable.message?:"can't swapper mgo server for ${opRequest.nameSpace}")
    }

  }

  override fun close() {
    synchronized(this) {
      this.isClosed.set(true)
      this.netSocketWrapperFactory.close()
    }
  }

  private fun swapper(opRequest: OpRequest,buffer: Buffer,netSocketWrapper: NetSocketWrapper) {
    when(netSocketWrapper.dataSourceStatus) {
      DataSourceStatus.ENABLE -> {
        netSocketWrapper.write(buffer)
      }
      DataSourceStatus.READONLY -> {
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
    private val mgoNamespaces :MgoNamespacePandora

    init {
      mgoNamespaces = MgoNamespacePandora()
    }

    fun createSocketWrapper(collectionName: String):CompletableFuture<NetSocketWrapper> {
      return CompletableFuture.supplyAsync {
         val mgoDatabase = mgoNamespaces.getMgoDatabase(collectionName)
         if(null == mgoDatabase) {
             throw IllegalStateException("can't get ${collectionName} mgo address from pandora.")
         }
         mgoDatabase
      }
        .thenApply {
          val mgoDatabase = it;
          val mgoCluster = it.getMgoCluster()
          val urls = mgoCluster.getUrlsAsString()
          serverSockets.forEach { k, v ->
            if (v.serverUrls.equals(urls)) {
              serverSockets.putIfAbsent(collectionName, v)
            }
          }
          val netSocketWrapper = serverSockets.get(collectionName)
          netSocketWrapper?:let {

            val hostAndPort = findMaster(mgoCluster)
             hostAndPort?.let {
                 val netSocketWrapper_tmp =  NetSocketWrapper(collectionName,mgoDatabase,it,netClient,{this.onClose(it)})
                 serverSockets.putIfAbsent(collectionName,netSocketWrapper_tmp)
               netSocketWrapper_tmp
             }?:throw MgoWrapperException("the replicaSetStatus error.${collectionName}")

          }
        }
    }

    fun getSocketWrapper(collectionName:String) : NetSocketWrapper? {
      val netSocketWrapper = serverSockets.get(collectionName)
      if(null != netSocketWrapper && null != netSocketWrapper.netClient) {
        serverSockets.remove(collectionName)
      }
      return netSocketWrapper
    }

    /**
     * doc https://docs.mongodb.com/manual/reference/replica-states/
     */
    private fun findMaster(mgoCluster:MgoCluster): HostAndPort? {
      val hostAndPorts = mgoCluster.listUrl()
      var mgoClient :MongoClient ?= null
      try{
        mgoClient = MongoClient(hostAndPorts.map { ServerAddress(it.host,it.port) },MongoClientOptions.builder().connectTimeout(10).build())
        val runCommand = mgoClient.getDatabase("admin").runCommand(Document("replSetGetStatus", 1))
        val members = runCommand.get("members") as List<Map<String,Any>>
        if(CollectionUtils.isEmpty(members)) {
          throw MgoWrapperException("can't get primary from ${hostAndPorts}")
        }
        var master = members.filter {
          Objects.equal(it.get("state"),1)
        }.map {
          val name = (it.get("name") as String ).split(":")
          ServerAddress(name[0],Integer.valueOf(name[1]))
        }
          .first()
        return HostAndPort(master.host,master.port)
      }catch (throwable :Throwable) {
        logger.error("can't get primary from ${hostAndPorts},error msg:${throwable.message}",throwable)
        throw MgoWrapperException("can't get primary from ${hostAndPorts}")
      }finally {
        mgoClient?.let {
          it.close()
        }
      }
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
      logger.info("close all mgo transport connection:${serverSockets.keys}")
      synchronized(this) {
        serverSockets.forEach({it.value.close()})
        serverSockets.clear()
      }
    }
  }

  inner class NetSocketWrapper(val nameSpace:String, val mgoDatabase: MgoDatabase, val hostAndPort: HostAndPort, val netClient: NetClient,val closed:Callback<NetSocketWrapper>?=null):Closeable {

    private var socket: NetSocket? = null
    val serverUrls:String
    var dataSourceStatus:DataSourceStatus
    init {
      serverUrls = mgoDatabase.mgoCluster.urlsAsString
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
      val status = mgoDatabase.getStatus()
      dataSourceStatus = DataSourceStatus.valueOf(status.getValAsString())
      status.addDataChangedListener("kotlin_status_changed", {
        type, newValue ->
        logger.info("changed")
        dataSourceStatus = DataSourceStatus.valueOf(status.getValAsString())
      })
    }

    fun connect(): CompletableFuture<NetSocketWrapper>{
      var cf = CompletableFuture<NetSocketWrapper>()
      netClient.connect(hostAndPort.port,hostAndPort.host, {
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
            /**
             * TODO retry
             * 主动关闭和被动关闭都会调用 想把发把被动关闭retry
             */
            closed?.invoke(this)
            this.socket = null
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
          logger.error("can't  connect nameSpace ${nameSpace} cause:${cause.message}",cause)
          cf.obtrudeException(cause)
        }
      })
      return  cf
    }

    fun write(buffer:Buffer) {
      socket?.write(buffer) ?: throw MgoWrapperException("can't connect  mgo server for ${nameSpace}.")
    }

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
  }

}