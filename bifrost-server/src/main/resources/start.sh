#!/bin/bash

if  [ -z $HOST];then
vertx run java-hk2:com.maxleap.bifrost.kotlin.BifrostServer --conf config.json $@
else
vertx run java-hk2:com.maxleap.bifrost.kotlin.BifrostServer --conf config.json -cluster -cluster-host $HOST $@
fi