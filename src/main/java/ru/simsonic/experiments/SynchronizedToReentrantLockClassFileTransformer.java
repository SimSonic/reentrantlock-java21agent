package ru.simsonic.experiments;

import lombok.SneakyThrows;
import org.objectweb.asm.ClassReader;

import java.lang.instrument.ClassFileTransformer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.ProtectionDomain;

public class SynchronizedToReentrantLockClassFileTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(
            ClassLoader loader,
            String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain,
            byte[] classFileBuffer
    ) {
        return loader != null
               ? transformClass(loader, classFileBuffer)
               : classFileBuffer;
    }

    public static byte[] transformClass(ClassLoader loader, byte[] classFileBuffer) {
        var reader = new ClassReader(classFileBuffer);
        var writer = new SynchronizedToReentrantLockClassFileWriter(reader);
        var visitor = new SynchronizedToReentrantLockClassVisitor(loader, writer, classFileBuffer);
        reader.accept(visitor, 0);
        if (visitor.isIntroducedChanges()) {
            byte[] modifiedClassFileBuffer = writer.toByteArray();
            String fileName = "target/transformed-classes/" + visitor.getClassName().replace('.', '/') + ".class";
            saveBytes(fileName, modifiedClassFileBuffer);
            return modifiedClassFileBuffer;
        } else {
            return classFileBuffer;
        }
    }

    @SneakyThrows
    private static void saveBytes(String file, byte[] bytes) {
        Path path = Path.of(file);
        Files.createDirectories(path.getParent());
        Files.write(path, bytes);
    }
}
