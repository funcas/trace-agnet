# 灰度发布agent

## 使用方法

* 适用范围：使用vv-fw构建的spring-cloud微服务应用（因为编译版本使用的是fw继承下来的）
* 对于网关，简单添加本agent到启动命令行即可,如下示例
```shell
java -jar -javaagent:vv-agnet-1.0-SNAPSHOT-jar-with-dependencies.jar xxx-service.jar 
```

* 对于其它服务，除了添加本agent，还需要添加阿里ttl的agent。注意将ttl的agnet放到第一个，防止与skywalking冲突。

```shell
java -jar -javaagent:transmittable-thread-local-2.13.2.jar -javaagent:vv-agnet-1.0-SNAPSHOT-jar-with-dependencies.jar xxx-service.jar 
```


## 影响面

本Agent的设计旨在启动期间动态加载组件增强策略至Spring上下文中，而不是拦截方法数据，在字节码层面直接修改，保证了安全性、扩展性和可维护性。
Agent注入成功后，会自动加载如下策略：

* FeignRequest拦截器，用于给feign请求头加上灰度标识
* RestTemplate拦截器，用户给resttemplate发起的请求头加上灰度标识
* Ribbon自定义负载均衡策略，此为核心原理，在负载均衡层面，按灰度标识来选取对应的服务分发请求
* SpringWebMvc拦截器，用于拦截Controller的入口流量，给线程池的线程加上灰度标识，用于线程间标识透传
* Spring Cloud LoadBalancer自定义负载均衡器，目前仅网关专用，用于网关的灰度流量分发

理论上因为多了一层拦截操作，会丢失少部分性能，但都是简单判断与赋值型的O(1)操作，对性能的影响较小。ttl会透传流量标识，目前仅一个字符串，对内存影响较小。