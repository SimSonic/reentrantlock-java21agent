package ru.simsonic.experiments;

import lombok.SneakyThrows;

import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;

@SuppressWarnings("unused")
public class AgentMain {

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        addToClassPathSearch(instrumentation);
        instrumentation.addTransformer(new SynchronizedToReentrantLockClassFileTransformer());
    }

    @SneakyThrows
    private static void addToClassPathSearch(Instrumentation instrumentation) {
        String jarFile = AgentMain.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(jarFile));
    }
}
