package cn.vv.agent.spring.rabbit;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * TODO
 *
 * @author Shane Fang
 * @since 1.0
 */
public class RabbitTemplatePostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof RabbitTemplate) {
            RabbitTemplate template = (RabbitTemplate) bean;
            template.setBeforePublishPostProcessors(new RabbitMsgBeforePostProcessor());
            template.setAfterReceivePostProcessors(new RabbitMsgAfterPostProcessor());
        }

        return bean;
    }
}
