package cn.vv.agent;

import cn.vv.agent.loader.Archive;
import cn.vv.agent.loader.JarFileArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.List;

/**
 * TODO
 *
 * @author Shane Fang
 * @since 1.0
 */
public class VvTransformer implements ClassFileTransformer {
    public static final Logger logger = LoggerFactory.getLogger(VvTransformer.class);
    private Instrumentation inst;

    public VvTransformer(Instrumentation inst) {
        this.inst = inst;
    }
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if ("org/springframework/boot/SpringApplication".equals(className)) {
            appendAgentNestedJars(loader);
        }
        return classfileBuffer;
    }

    /**
     * 获取 agent jar 的路径，抄springboot的
     */
    private String getAgentJarPath() {
        ProtectionDomain protectionDomain = getClass().getProtectionDomain();
        CodeSource codeSource = protectionDomain.getCodeSource();
        URI location = null;
        try {
            location = (codeSource == null ? null : codeSource.getLocation().toURI());
        } catch (URISyntaxException e) {
            return null;
        }
        String path = (location == null ? null : location.getSchemeSpecificPart());
        if (path == null) {
            throw new IllegalStateException("Unable to determine code source");
        }
        File root = new File(path);
        if (!root.exists()) {
            throw new IllegalStateException(
                    "Unable to determine code source from " + root);
        }
        return path;
    }

    private void appendAgentNestedJars(ClassLoader classLoader) {
        String agentJarPath = getAgentJarPath();
        if (agentJarPath == null) return;

        //LaunchedURLClassLoader 是属于 springboot-loader 的类，没有放到jar in jar里边，所以它是被AppClassLoader加载的
        if (classLoader instanceof URLClassLoader) {
            URLClassLoader launchedURLClassLoader = (URLClassLoader) classLoader;
            try {
                Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                method.setAccessible(true);
                //遍历 agent jar，处理所有对应目录下的jar包，使用 JarFileArchive 获取到的url才可以处理jar in jar
                JarFileArchive jarFileArchive = new JarFileArchive(new File(agentJarPath));
                List<Archive> archiveList = jarFileArchive.getNestedArchives(entry -> {
                    if (entry.isDirectory()) {
                        return false;
                    }
                    return entry.getName().startsWith("BOOT-INF/lib/") && entry.getName().endsWith(".jar");
                });

                for (Archive archive : archiveList) {
                    logger.info("add url to classloader. url: {}", archive.getUrl());
                    method.invoke(launchedURLClassLoader, archive.getUrl());
                }
            } catch (Throwable t) {
                logger.error(null, t);
            }
        }

    }
}
