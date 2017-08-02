package com.maxleap.bifrost.kotlin.api

/**
 * Created by.
 * User: ben
 * Date: 02/08/2017
 * Time: 4:16 PM
 * Email:benkris1@126.com
 *
 */
interface NamespaceFactory {

  fun loadNamespace(db:String):Namespace
}