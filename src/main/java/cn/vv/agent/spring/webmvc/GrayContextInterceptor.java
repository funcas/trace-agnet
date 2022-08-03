package cn.vv.agent.spring.webmvc;

import cn.vv.agent.common.Constants;
import cn.vv.agent.context.VvTraceContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * spring mvc拦截器，拦截controller入口，设置ctx与清除ctx
 *
 * @author Shane Fang
 * @since 1.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GrayContextInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler) {
        String xVersion = request.getHeader(Constants.KEY_HTTP_HEADER_VERSION);
        if (xVersion != null && !"".equals(xVersion)) {
            VvTraceContext.getCurrentContext().setVersion(xVersion);
        }
        return true;
    }


    @Override
    public void afterCompletion(@Nonnull HttpServletRequest request,
                                @Nonnull HttpServletResponse response,
                                @Nonnull Object handler,
                                Exception ex) {
        VvTraceContext.clearContext();
    }
}
