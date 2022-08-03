package cn.vv.agent.spring.resttemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Nonnull;
import java.util.Collections;

/**
 * 对RestTemplate Bean做增强处理，在bean实例化阶段动态添加过滤器，添加头信息
 *
 * @author Shane Fang
 * @since 1.0
 */
public class RestTemplateBeanPostProcessor implements BeanPostProcessor {

    public static final Logger logger = LoggerFactory.getLogger(RestTemplateInterceptor.class);
    @Override
    public Object postProcessBeforeInitialization(@Nonnull Object bean, @Nonnull String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(@Nonnull Object bean, @Nonnull String beanName) throws BeansException {
        if (bean instanceof RestTemplate) {
            logger.info("[restTemplate] - modify restTemplate bean to add interceptors");
            RestTemplate restTemplate = (RestTemplate) bean;
            restTemplate.setInterceptors(Collections.singletonList(new RestTemplateInterceptor()));
            logger.info("[restTemplate] - modify restTemplate bean to add interceptors done");
        }
        return bean;
    }

}
