#### Mongo Proxy —Bifrost-kotlin



![xxx](http://csqncdn.maxleap.cn/NTgwZDdiZTQ3ZTJjNzkwMDA3NDVhOWQ3/qn-f3dd75df-37e9-4020-a416-beb1b170783b.MOV)

Bifrost 是基于vertx3 开发的mongo代理服务器，使用kotlin编写。

##### 背景

由于Sass后台各个服务中间件需要访问mongo越来越多，mongo集群数量也逐渐增加。为了管理和监控方便，必须要统一mongo访问。Bifrost 对开发者屏蔽了后端mongo架构，直接访问bifrost即可。

##### 项目原理

- 通过解析 [mongo 协议](https://docs.mongodb.com/v3.0/reference/mongodb-wire-protocol/)	实现 [mongo SCRAM](https://www.mongodb.com/blog/post/improved-password-based-authentication-mongodb-30-scram-explained-part-1?jmp=docs&_ga=2.113628933.303872216.1498450526-215400923.1486350235) 认证协议，达到开发者访问mongo的安全隔离
- 通过实现Namespace 获取db和cluster之间的关系
- 通过拦截mongo协议请求，实现mongo慢查询监控



##### 部署方法

本项目默认会自动打包成docker容器,images名称以bifrost-server:version的形式存放在registry里.
通过docker命令`docker run --name bifrost-kotlin --net=host -d bifrost-server:0.0.1-SNAPSHOT` 默认服务占用`271017`

```shell
$ mvn clean install -Dmaven.test.skip=true  
```



##### 开始使用

1.首先启动`bifrost`服务，可以使用docker启动或者在项目中执行Bootstrap.kt。 默认占用端口为`27017`,可以自行更改配置信息 `config.json`	。用户需要配置代理mongo 服务器地址，本项目是写死的，在`DefaultNamespace.kt`中，可以根据自己的业务实现。**目前仅支持mongo3.0版本**

2.模拟mongo终端登录

```shell
$ ./mongo localhost:27017/geo_blocks -u "username" -p "password" --authenticationDatabase "geo_blocks" --authenticationMechanism SCRAM-SHA-1
```

![term](http://csqncdn.maxleap.cn/NTgwZDdiZTQ3ZTJjNzkwMDA3NDVhOWQ3/qn-0622b7fb-c85a-41b1-be63-1fca64bd752c.MOV)

支持标准mongo驱动程序访问。默认用户名&密码:bifrost/bifrost

##### 监控

bifrost-kotlin 会记录操作耗时大于10ms的请求，使用Es或者OpenTsDB来实现打点监控。效果如下：

![term](http://csqncdn.maxleap.cn/NTgwZDdiZTQ3ZTJjNzkwMDA3NDVhOWQ3/qn-59d39d79-5b87-40b3-96df-382f39124582.MOV)



