#!/bin/bash

if  [ -z $HOST];then
vertx run java-hk2:as.leap.scylla.kotlin.impl.ScyllaVerticle --conf config.json $@
else
vertx run java-hk2:as.leap.scylla.kotlin.impl.ScyllaVerticle --conf config.json -cluster -cluster-host $HOST $@
fi