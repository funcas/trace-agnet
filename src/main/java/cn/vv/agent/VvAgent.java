package cn.vv.agent;

import cn.vv.agent.interceptor.DiscoveryImportInterceptor;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;

/**
 * Agent 入口类
 *
 * @author Shane Fang
 * @since 1.0
 */
public class VvAgent {

    /**
     * jvm 参数形式启动，运行此方法
     *
     * @param agentArgs
     * @param inst
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        new AgentBuilder.Default()
                .type(ElementMatchers.named("org.springframework.cloud.client.discovery.EnableDiscoveryClientImportSelector"))
                .transform((builder, typeDescription, classLoader, module) -> builder
                        .method(ElementMatchers.named("selectImports"))
                        .intercept(MethodDelegation.to(DiscoveryImportInterceptor.class))
                )
                .installOn(inst);
    }

    /**
     * 动态 attach 方式启动，运行此方法
     * 此方法没有实现，因为动态扩展spring bean注册，不在启动阶段无法生效
     *
     * @param agentArgs
     * @param inst
     */
    public static void agentmain(String agentArgs, Instrumentation inst) {
        System.out.println("agentmain");
    }
}
