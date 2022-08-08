package cn.vv.agent.context;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * 全链路上下文持有类，使用阿里的ttl工具，需要配置ttl agent使用，否则对线程池不生效
 * 在agent管理范围内会自动清理，如果手工使用，请注意及时清理ctx的内容，否则可能造成内存泄露
 *
 * @author Shane Fang
 * @since 1.0
 */
public class VvTraceContext {

    private static final TransmittableThreadLocal<TraceContent> holder = TransmittableThreadLocal.withInitial(TraceContent::new);

    public static TraceContent getCurrentContext() {
        return holder.get();
    }

    public static void setCurrentContext(TraceContent context) {
        holder.set(context);
    }

    public static void clearContext() {
        holder.remove();
    }
}
