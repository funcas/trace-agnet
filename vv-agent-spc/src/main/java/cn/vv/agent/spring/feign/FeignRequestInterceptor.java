package cn.vv.agent.spring.feign;

import cn.vv.agent.common.Constants;
import cn.vv.agent.context.VvTraceContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OpenFeign拦截器，拦截ctx内的Version信息，并放头feign头透传下去
 *
 * @author Shane Fang
 * @since 1.0
 */
public class FeignRequestInterceptor implements RequestInterceptor {
    public static final Logger logger = LoggerFactory.getLogger(FeignRequestInterceptor.class);
    @Override
    public void apply(RequestTemplate template) {
        if(logger.isDebugEnabled()) {
            logger.info("[FEIGN] - feign interceptor got version := {}", VvTraceContext.getCurrentContext().getVersion());
        }
        template.header(Constants.KEY_HTTP_HEADER_VERSION, VvTraceContext.getCurrentContext().getVersion());
    }
}