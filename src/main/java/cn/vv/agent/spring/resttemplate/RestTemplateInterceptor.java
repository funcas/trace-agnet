package cn.vv.agent.spring.resttemplate;

import cn.vv.agent.common.Constants;
import cn.vv.agent.context.VvTraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * resttemplate拦截器，将ctx里的version置入http头，该场景workflow服务使用
 *
 * @author Shane Fang
 * @since 1.0
 */
public class RestTemplateInterceptor implements ClientHttpRequestInterceptor {

    public static final Logger logger = LoggerFactory.getLogger(RestTemplateInterceptor.class);
    @Override
    @Nonnull
    public ClientHttpResponse intercept(@Nonnull HttpRequest request,
                                        @Nonnull byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        addRequestHeader(request);
        return execution.execute(request, body);
    }

    // 向头部中加入灰度标记
    private void addRequestHeader(HttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        String version = VvTraceContext.getCurrentContext().getVersion();
        logger.info("[restTemplate] - set version := {}", version);
        headers.add(Constants.KEY_HTTP_HEADER_VERSION,version);
    }
}
