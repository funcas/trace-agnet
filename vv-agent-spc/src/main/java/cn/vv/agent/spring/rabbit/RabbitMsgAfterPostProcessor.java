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
public class RabbitMsgAfterPostProcessor implements MessagePostProcessor {
    @Override
    public Message postProcessMessage(Message message) throws AmqpException {
//        message.getMessageProperties().setHeader("x-version", TraceContext.getCurrentContext().get("version"));
        String xVersion = (String)message.getMessageProperties().getHeaders().get(Constants.KEY_HTTP_HEADER_VERSION);
        System.out.println("" + xVersion);
        VvTraceContext.getCurrentContext().setVersion(xVersion);
        return message;
    }
}
