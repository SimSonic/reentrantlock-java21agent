package ru.simsonic.experiments;

import lombok.Getter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import ru.simsonic.experiments.SynchronizedToReentrantLockContextClassVisitor.OriginalMethodContext;

import java.util.List;
import java.util.Map;

class SynchronizedToReentrantLockClassVisitor extends ClassVisitor {

    private static final List<String> PACKAGES_TO_SKIP = List.of(
            "java.",
            "sun.",
            "jdk.",
            "javax.management.",
            "com.sun.management.internal.",
            "com.sun.xml.messaging.saaj.packaging.mime.MessagingException",
            "org.apache.http.",
            "com.amazonaws.",
            "com.github.luben.zstd.",
            "com.esri.core.",
            "net.iakovlev.timeshape.",
            "org.xnio.",
            "reactor.core.",
            "com.intellij"
    );

    private final ClassLoader classLoader;
    private final Map<String, OriginalMethodContext> monitorEntersPerMethod;

    @Getter
    private String className;
    @Getter
    private boolean introducedChanges = false;

    SynchronizedToReentrantLockClassVisitor(ClassLoader classLoader, ClassVisitor cv, byte[] originalClassFileBuffer) {
        super(Opcodes.ASM9, cv);
        this.classLoader = classLoader;
        this.monitorEntersPerMethod = fillMethodsContexts(originalClassFileBuffer);
    }

    private Map<String, OriginalMethodContext> fillMethodsContexts(byte[] originalClassFileBuffer) {
        var classReader = new ClassReader(originalClassFileBuffer);
        var classVisitor = new SynchronizedToReentrantLockContextClassVisitor();
        classReader.accept(classVisitor, 0);
        return classVisitor.getResult();
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name.replace('/', '.');
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        String uniqueMethodKey = OriginalMethodContext.uniqueMethodKey(name, desc, signature);
        OriginalMethodContext context = monitorEntersPerMethod.get(uniqueMethodKey);

        boolean isMethodSynchronized = context.isMethodSynchronized();
        if (isMethodSynchronized) {
            access &= ~Opcodes.ACC_SYNCHRONIZED;
        }

        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);

        if (context.isModifiable() && !skipClassByPackage()) {
            System.out.println("Transforming: " + this.className + "#" + uniqueMethodKey + " loaded by " + classLoader.getName());
            introducedChanges = true;
            return new SynchronizedToReentrantLockMethodVisitor(mv, context);
        }

        return mv;
    }

    private boolean skipClassByPackage() {
        return PACKAGES_TO_SKIP.stream().anyMatch(p -> this.className.startsWith(p));
    }
}
