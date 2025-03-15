package ru.simsonic.experiments.utils;

import lombok.SneakyThrows;
import ru.simsonic.experiments.SynchronizedToReentrantLockClassFileTransformer;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class TestClassTransformer {

    public static byte[] transformClass(Class<?> clazz) {
        String classSimpleName = clazz.getSimpleName();

        byte[] originalClassFileBuffer = getClassBytes(clazz);
        String file1 = "src/test/resources/%s-original.class".formatted(classSimpleName);
        saveBytes(file1, originalClassFileBuffer);
        System.out.println("Saved file: " + file1);

        byte[] transformedClassFileBuffer = SynchronizedToReentrantLockClassFileTransformer.transformClass(
                ClassLoader.getSystemClassLoader(),
                originalClassFileBuffer
        );
        String file2 = "src/test/resources/%s-transformed.class".formatted(classSimpleName);
        saveBytes(file2, transformedClassFileBuffer);
        System.out.println("Saved file: " + file2);

        return transformedClassFileBuffer;
    }

    @SneakyThrows
    private static byte[] getClassBytes(Class<?> clazz) {
        String classPathResourceName = clazz.getName().replace('.', '/') + ".class";
        try (InputStream is = clazz.getClassLoader().getResourceAsStream(classPathResourceName)) {
            return Objects.requireNonNull(is).readAllBytes();
        }
    }

    @SneakyThrows
    private static void saveBytes(String file, byte[] bytes) {
        Path path = Path.of(file);
        Files.createDirectories(path.getParent());
        Files.write(path, bytes);
    }
}
