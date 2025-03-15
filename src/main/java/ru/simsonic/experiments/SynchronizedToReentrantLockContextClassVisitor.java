package ru.simsonic.experiments;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.simsonic.experiments.SynchronizedToReentrantLockContextClassVisitor.OriginalMethodContext.uniqueMethodKey;

class SynchronizedToReentrantLockContextClassVisitor extends ClassVisitor {

    private final Map<String, OriginalMethodContext> result = new HashMap<>();

    SynchronizedToReentrantLockContextClassVisitor() {
        super(Opcodes.ASM9);
    }

    public Map<String, OriginalMethodContext> getResult() {
        return Collections.unmodifiableMap(result);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        String uniqueMethodKey = uniqueMethodKey(name, desc, signature);
        boolean isMethodSynchronized = (access & Opcodes.ACC_SYNCHRONIZED) != 0;
        OriginalMethodContext context = OriginalMethodContext.create(isMethodSynchronized);
        result.put(uniqueMethodKey, context);
        return new SynchronizedToReentrantLockContextMethodVisitor(context);
    }

    record OriginalMethodContext(boolean isMethodSynchronized, AtomicInteger monitorCount, AtomicInteger localVariablesCount) {

        private static OriginalMethodContext create(boolean isMethodSynchronized) {
            return new OriginalMethodContext(isMethodSynchronized, new AtomicInteger(), new AtomicInteger());
        }

        boolean isModifiable() {
            return isMethodSynchronized || monitorCount().get() > 0;
        }

        static String uniqueMethodKey(String name, String desc, String signature) {
            return "%s-%s-%s".formatted(name, desc, signature);
        }
    }
}
