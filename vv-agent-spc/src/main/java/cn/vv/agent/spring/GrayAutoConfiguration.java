package cn.vv.agent.spring;

import cn.vv.agent.spring.feign.FeignRequestInterceptor;
import cn.vv.agent.spring.gw.GrayRoundRobinLoadBalancer;
import cn.vv.agent.spring.rabbit.RabbitListenerContainerFactoryPostProcessor;
import cn.vv.agent.spring.rabbit.RabbitTemplatePostProcessor;
import cn.vv.agent.spring.resttemplate.RestTemplateBeanPostProcessor;
import cn.vv.agent.spring.ribbon.VvGrayRule;
import cn.vv.agent.spring.webmvc.GrayContextInterceptor;
import com.netflix.loadbalancer.ZoneAvoidanceRule;
import org.springframework.amqp.rabbit.config.AbstractRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClientConfigurationRegistrar;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Nonnull;

/**
 * 动态注册进spring容器的 autoconfiguration
 *
 * @author Shane Fang
 * @since 1.0
 */
@Configuration
public class GrayAutoConfiguration {

    /**
     * 非网关部分的bean增强注册自动配置
     */
    @Configuration
    @ConditionalOnClass(ZoneAvoidanceRule.class)
    public static class ServiceAutoConfiguration {
        @Bean
        public FeignRequestInterceptor feignRequestInterceptor() {
            return new FeignRequestInterceptor();
        }

        @Bean
        public RestTemplateBeanPostProcessor restTemplateBeanPostProcessor() {
            return new RestTemplateBeanPostProcessor();
        }

        @Bean
        public VvGrayRule grayRule() {
            return new VvGrayRule();
        }

        @Bean
        public GrayContextInterceptor grayContextInterceptor() {
            return new GrayContextInterceptor();
        }

        @Bean
        public WebMvcConfigurer createWebMvcConfigurer(GrayContextInterceptor interceptor) {
            return new WebMvcConfigurer() {
                @Override
                public void addInterceptors(@Nonnull InterceptorRegistry registry) {
                    registry.addInterceptor(interceptor);
                }
            };
        }

//        @Bean
//        @ConditionalOnMissingBean(name = "feignHystrixConcurrencyStrategy")
//        public FeignHystrixConcurrencyStrategy feignHystrixConcurrencyStrategy() {
//            return new FeignHystrixConcurrencyStrategy();
//        }
    }


    /*============================ Spring Cloud Gateway 使用以下自动配置 ===============================*/

    @Configuration
    @ConditionalOnClass(RoundRobinLoadBalancer.class)
    public static class ReactorLoadBalancerAutoConfiguration {

        /**
         * 注册自定义响应式负载均衡策略
         * @param environment
         * @param serviceInstanceListSupplierProvider
         * @return
         */
        @Bean
        public ReactorServiceInstanceLoadBalancer reactorServiceInstanceLoadBalancer(Environment environment,
                                                                                     ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider){
            String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
            return new GrayRoundRobinLoadBalancer(serviceInstanceListSupplierProvider, name);
        }
    }

    /**
     * 全局注册负载均衡策略，此处优化级会比网关代码上直接写上去的低，所以在代码上注册后，此处注解会失效
     * 后续考虑直接拦截 {@link LoadBalancerClientConfigurationRegistrar#registerClientConfiguration(BeanDefinitionRegistry, Object, Object)}
     * 进行动态注册，但对字节码有侵入
     */
    @Configuration
    @AutoConfigureAfter(ReactorLoadBalancerAutoConfiguration.class)
    @ConditionalOnClass(RoundRobinLoadBalancer.class)
    @LoadBalancerClients(defaultConfiguration = ReactorLoadBalancerAutoConfiguration.class)
    public static class GwLoadBalancerRegisterConfiguration {
    }


    @ConditionalOnClass(AbstractRabbitListenerContainerFactory.class)
    @Bean
    public RabbitListenerContainerFactoryPostProcessor rabbitListenerContainerFactoryPostProcessor() {
        return new RabbitListenerContainerFactoryPostProcessor();
    }

    @Bean
    @ConditionalOnClass(RabbitTemplate.class)
    public RabbitTemplatePostProcessor rabbitTemplatePostProcessor() {
        return new RabbitTemplatePostProcessor();
    }

}
