FROM 10.10.10.160:8010/maxleap/base:${maxleap-stack.version}
MAINTAINER Ben.Ma <cma@maxleap.com>

#----------------------------Copy 项目目录到容器里--------------------------------------------

RUN \
mkdir -p /opt/bifrost-kotlin

#覆盖vert.x 相关配置
ADD lib/ $VERTX_HOME/lib/
ADD log4j2.xml $VERTX_HOME/conf
ADD zookeeper.properties $VERTX_HOME/conf
ADD zookeeper.json $VERTX_HOME/conf
ADD config.json /opt/bifrost-kotlin/
ADD start.sh /opt/bifrost-kotlin/

WORKDIR /opt/bifrost-kotlin
ENTRYPOINT ["./start.sh"]
