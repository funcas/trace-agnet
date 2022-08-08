package cn.vv.agent.spring.gw;

import cn.vv.agent.common.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * sprong cloud loadbalance 自定义负载均衡器，网关专用
 *
 * @author Shane Fang
 * @since 1.0
 */
public class GrayRoundRobinLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    public static final Logger logger = LoggerFactory.getLogger(GrayRoundRobinLoadBalancer.class);
    private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    private final String serviceId;
    private final AtomicInteger position;


    public GrayRoundRobinLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, String serviceId) {
        this(serviceInstanceListSupplierProvider, serviceId, new Random().nextInt(1000));
    }

    public GrayRoundRobinLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, String serviceId, int seedPosition) {
        this.serviceId = serviceId;
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.position = new AtomicInteger(seedPosition);
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        DefaultRequestContext requestContext = (DefaultRequestContext) request.getContext();
        RequestData clientRequest = (RequestData) requestContext.getClientRequest();
        HttpHeaders headers = clientRequest.getHeaders();
        ServiceInstanceListSupplier supplier = this.serviceInstanceListSupplierProvider
                .getIfAvailable(NoopServiceInstanceListSupplier::new);
        return supplier.get(request).next().map(serviceInstances -> processInstanceResponse(serviceInstances, headers));
    }

    private Response<ServiceInstance> processInstanceResponse(List<ServiceInstance> instances, HttpHeaders headers) {
        if (instances.isEmpty()) {
            return new EmptyResponse();
        } else {
            String reqVersion = headers.getFirst(Constants.KEY_HTTP_HEADER_VERSION);
            if(logger.isDebugEnabled()) {
                logger.debug("[GW] - header x-version := {}", reqVersion);
            }
            if (StringUtils.isEmpty(reqVersion)) {
                return processRibbonInstanceResponse(instances);
            }

            List<ServiceInstance> serviceInstances = instances.stream()
                    .filter(instance -> reqVersion.equals(instance.getMetadata().get(Constants.KEY_METADATA_VERSION)))
                    .collect(Collectors.toList());

            if (serviceInstances.size() > 0) {
                return processRibbonInstanceResponse(serviceInstances);
            }

            return processRibbonInstanceResponse(instances);

        }
    }

    /**
     * 负载均衡器,
     * 参考 {@link org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer#getInstanceResponse(List)}
     */
    private Response<ServiceInstance> processRibbonInstanceResponse(List<ServiceInstance> instances) {
        int pos = Math.abs(this.position.incrementAndGet());
        ServiceInstance instance = instances.get(pos % instances.size());
        return new DefaultResponse(instance);
    }
}
