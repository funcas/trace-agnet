package cn.vv.agent.spring.rabbit;

import cn.vv.agent.common.Constants;
import cn.vv.agent.context.VvTraceContext;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;

/**
 * TODO
 *
 * @author Shane Fang
 * @since 1.0
 */
public class RabbitMsgBeforePostProcessor implements MessagePostProcessor {
    @Override
    public Message postProcessMessage(Message message) throws AmqpException {
        System.out.println("before");
        message.getMessageProperties().setHeader(Constants.KEY_HTTP_HEADER_VERSION,
                VvTraceContext.getCurrentContext().getVersion());

        return message;
    }
}
