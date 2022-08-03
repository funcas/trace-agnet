package cn.vv.agent.spring;

import cn.vv.agent.spring.feign.FeignRequestInterceptor;
import cn.vv.agent.spring.gw.GrayRoundRobinLoadBalancer;
import cn.vv.agent.spring.resttemplate.RestTemplateBeanPostProcessor;
import cn.vv.agent.spring.ribbon.VvGrayRule;
import cn.vv.agent.spring.webmvc.GrayContextInterceptor;
import com.netflix.loadbalancer.ZoneAvoidanceRule;
import feign.RequestInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.loadbalancer.config.LoadBalancerAutoConfiguration;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Nonnull;

/**
 * 动态注册进spring容器的 autoconfiguration
 *
 * @author Shane Fang
 * @since 1.0
 */
public class GrayAutoConfiguration {

    @Bean
    @ConditionalOnClass(RequestInterceptor.class)
    public FeignRequestInterceptor feignRequestInterceptor() {
        return new FeignRequestInterceptor();
    }

    @Bean
    @ConditionalOnClass(RestTemplate.class)
    public RestTemplateBeanPostProcessor restTemplateBeanPostProcessor() {
        return new RestTemplateBeanPostProcessor();
    }

    @Bean
    @ConditionalOnClass(ZoneAvoidanceRule.class)
    public VvGrayRule grayRule() {
        return new VvGrayRule();
    }

    @Bean
    @ConditionalOnClass(HandlerInterceptor.class)
    public GrayContextInterceptor grayContextInterceptor() {
        return new GrayContextInterceptor();
    }

    @Bean
    @ConditionalOnClass(HandlerInterceptor.class)
    public WebMvcConfigurer createWebMvcConfigurer(GrayContextInterceptor interceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(@Nonnull InterceptorRegistry registry) {
                registry.addInterceptor(interceptor);
            }
        };
    }

    @Bean
    @ConditionalOnClass(LoadBalancerAutoConfiguration.class)
    public ReactorServiceInstanceLoadBalancer reactorServiceInstanceLoadBalancer(Environment environment,
                                                                                 ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider){
        String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        return new GrayRoundRobinLoadBalancer(serviceInstanceListSupplierProvider, name);
    }
}
