package cn.vv.agent.spring.rabbit;

import org.springframework.amqp.rabbit.config.AbstractRabbitListenerContainerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * TODO
 *
 * @author Shane Fang
 * @since 1.0
 */
public class RabbitListenerContainerFactoryPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof AbstractRabbitListenerContainerFactory) {
            AbstractRabbitListenerContainerFactory containerFactory = (AbstractRabbitListenerContainerFactory)bean;
            containerFactory.setAfterReceivePostProcessors(new RabbitMsgAfterPostProcessor());
        }

        return bean;
    }
}
