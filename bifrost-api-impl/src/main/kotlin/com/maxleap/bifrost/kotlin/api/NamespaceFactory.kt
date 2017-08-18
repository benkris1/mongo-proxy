package com.maxleap.bifrost.kotlin.api

/**
 * Created by.
 * User: ben
 * Date: 02/08/2017
 * Time: 4:16 PM
 * Email:benkris1@126.com
 * a factory for get mongo namespace
 * @see PanddoraNamespaceFactory
 */
interface NamespaceFactory {

  fun loadNamespace(db:String):Namespace
}