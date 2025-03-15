package ru.simsonic.experiments;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

class SynchronizedToReentrantLockClassFileWriter extends ClassWriter {

    private static final String JAVA_LANG_OBJECT = "java/lang/Object";

    SynchronizedToReentrantLockClassFileWriter(ClassReader reader) {
        super(reader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
    }

    @Override
    protected String getCommonSuperClass(final String type1, final String type2) {
        try {
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            Class<?> class1 = Class.forName(type1.replace('/', '.'), false, classLoader);
            Class<?> class2 = Class.forName(type2.replace('/', '.'), false, classLoader);
            return super.getCommonSuperClass(type1, type2);
        } catch (ClassNotFoundException ex) {
            // классы не были загружены, возвращаем вашу заглушку
            return (type1.equals(type2) || type1.equals(JAVA_LANG_OBJECT) || type2.equals(JAVA_LANG_OBJECT))
                   ? JAVA_LANG_OBJECT
                   : type1;
        }
    }
}
