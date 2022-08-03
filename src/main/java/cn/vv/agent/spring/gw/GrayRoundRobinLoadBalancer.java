package cn.vv.agent.spring.gw;

import cn.vv.agent.common.Constants;
import com.alibaba.cloud.nacos.NacosServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

/**
 * sprong cloud loadbalance 自定义负载均衡器，网关专用
 *
 * @author Shane Fang
 * @since 1.0
 */
public class GrayRoundRobinLoadBalancer extends RoundRobinLoadBalancer {

    public static final Logger logger = LoggerFactory.getLogger(GrayRoundRobinLoadBalancer.class);
    public GrayRoundRobinLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, String serviceId) {
        super(serviceInstanceListSupplierProvider, serviceId);
        this.serviceId = serviceId;
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
    }

    private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;

    private final String serviceId;

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider
                .getIfAvailable(NoopServiceInstanceListSupplier::new);
        return supplier.get(request).next().map(serviceInstances -> getInstanceResponse(serviceInstances, request));
    }

    Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> instances, @Nonnull Request request) {

        // 注册中心无可用实例 抛出异常
        if (CollectionUtils.isEmpty(instances)) {
            return new EmptyResponse();
        }

        DefaultRequestContext requestContext = (DefaultRequestContext) request.getContext();
        RequestData clientRequest = (RequestData) requestContext.getClientRequest();
        HttpHeaders headers = clientRequest.getHeaders();

        String reqVersion = headers.getFirst(Constants.KEY_HTTP_HEADER_VERSION);
        logger.info("[loadbalancer] - got version := {}", reqVersion);
        if (StringUtils.isEmpty(reqVersion)) {
            return super.choose(request).block();
        }

        for (ServiceInstance instance : instances) {
            NacosServiceInstance nacosInstance = (NacosServiceInstance) instance;
            Map<String, String> metadata = nacosInstance.getMetadata();
            String targetVersion = metadata.get(Constants.KEY_METADATA_VERSION);
            if (reqVersion.equalsIgnoreCase(targetVersion)) {
                return new DefaultResponse(nacosInstance);
            }
        }
        // 降级策略，使用轮询策略
        return super.choose(request).block();
    }
}
