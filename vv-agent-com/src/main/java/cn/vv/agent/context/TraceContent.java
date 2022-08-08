package cn.vv.agent.context;

import java.util.HashMap;
import java.util.Map;

/**
 * 上下文透传内容，请不要随意增加该内的载荷
 *
 * @author Shane Fang
 * @since 1.0
 */
public class TraceContent {

    private String version;
    private Map<String, String> ext = new HashMap<>();

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, String> getExt() {
        return ext;
    }

    public void setExt(Map<String, String> ext) {
        this.ext = ext;
    }
}
