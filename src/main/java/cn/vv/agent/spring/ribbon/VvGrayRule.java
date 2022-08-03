package cn.vv.agent.spring.ribbon;

import cn.vv.agent.common.Constants;
import cn.vv.agent.context.VvTraceContext;
import com.alibaba.cloud.nacos.ribbon.NacosServer;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAvoidanceRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ribbon loadbalance 自定义负载均衡器
 *
 * @author Shane Fang
 * @since 1.0
 */
public class VvGrayRule extends ZoneAvoidanceRule {

    public static final Logger logger = LoggerFactory.getLogger(VvGrayRule.class);
    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {
    }

    @Override
    public Server choose(Object key) {

        //从ThreadLocal中获取灰度标记
        String grayVersion = VvTraceContext.getCurrentContext().getVersion();
        logger.info("[ribbon] - version := {}", grayVersion);
        if (grayVersion == null || "".equals(grayVersion)) {
            grayVersion = Constants.VAL_VERSION_PROD;
        }
        //获取所有可用服务
        List<Server> serverList = this.getLoadBalancer().getReachableServers();

        Map<String, List<Server>> serverMap = serverList.stream().collect(Collectors.groupingBy(item -> {
            NacosServer server = (NacosServer) item;
            if (!server.getMetadata().containsKey(Constants.KEY_METADATA_VERSION)) {
                return Constants.VAL_VERSION_PROD;
            }
            return server.getMetadata().get(Constants.KEY_METADATA_VERSION);
        }));
        logger.info("[ribbon] - serviceInstanceMap := {}", serverMap);
        return originChoose(serverMap.get(grayVersion), key);


    }

    private Server originChoose(List<Server> noMetaServerList, Object key) {
        if (noMetaServerList == null) {
            return null;
        }

        return getPredicate().chooseRoundRobinAfterFiltering(noMetaServerList, key).orNull();
    }
}
