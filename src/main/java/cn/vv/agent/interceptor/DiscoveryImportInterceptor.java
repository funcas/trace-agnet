package cn.vv.agent.interceptor;

import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * bytebuddy代理类
 *
 * @author Shane Fang
 * @since 1.0
 */
public class DiscoveryImportInterceptor {

    public static final String AGENT_AUTOCONFIGURATION_CLASS = "cn.vv.agent.spring.configuration.GrayAutoConfiguration";

    @RuntimeType
    public static Object intercept(@Origin Method method, @SuperCall Callable<?> callable) throws Exception {

        String[] imports = (String[])callable.call();
        List<String> importsList = new ArrayList<>(Arrays.asList(imports));
        importsList.add("");
        return importsList.toArray(new String[0]);
    }
}
