package cn.vv.agent;

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
        VvTransformer transformer = new VvTransformer(inst);
        inst.addTransformer(transformer, true);

    }

    /**
     * 动态 attach 方式启动，运行此方法
     * 此方法没有实现，因为动态扩展spring bean注册，不在启动阶段无法生效,
     * 后续可以扩展类似vone的做法拦截生效类来完成attach时时生效能力，本期暂不实现
     *
     * @param agentArgs
     * @param inst
     */
    public static void agentmain(String agentArgs, Instrumentation inst) {
        // TODO: 待实现
        System.out.println("agentmain");
    }

}
