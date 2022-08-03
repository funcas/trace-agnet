package cn.vv.agent.spring.feign;

import cn.vv.agent.common.Constants;
import cn.vv.agent.context.VvTraceContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * OpenFeign拦截器，拦截ctx内的Version信息，并放头feign头透传下去
 *
 * @author Shane Fang
 * @since 1.0
 */
public class FeignRequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
//        System.out.println("feign interceptor => " + VvTraceContext.getCurrentContext().getVersion());
        template.header(Constants.KEY_HTTP_HEADER_VERSION, VvTraceContext.getCurrentContext().getVersion());
    }
}