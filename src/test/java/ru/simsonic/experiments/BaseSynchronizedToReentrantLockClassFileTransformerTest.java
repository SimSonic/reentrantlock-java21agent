package ru.simsonic.experiments;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import ru.simsonic.experiments.object.MyClass;
import ru.simsonic.experiments.object.MyInterface;
import ru.simsonic.experiments.utils.TestClassLoader;
import ru.simsonic.experiments.utils.TestClassTransformer;

import static org.assertj.core.api.Assertions.assertThat;

public class BaseSynchronizedToReentrantLockClassFileTransformerTest {

    protected static MyInterface myClassModifiedInstance;

    @SneakyThrows
    @BeforeAll
    static void beforeAll() {
        byte[] transformedBytes = TestClassTransformer.transformClass(MyClass.class);

        TestClassLoader testClassLoader = new TestClassLoader();

        //noinspection unchecked
        Class<MyClass> transformedClass = (Class<MyClass>) testClassLoader.defineClass(
                MyClass.class.getName(),
                transformedBytes
        );

        myClassModifiedInstance = transformedClass.getDeclaredConstructor().newInstance();
    }

    @AfterEach
    void afterEach() {
        System.gc();

        var debugMap = SharedReentrantLock.getDebugMap();
        int size = debugMap.size();
        System.out.println("size = " + size);

        assertThat(debugMap).hasSize(0);

        for (var lock : debugMap.values()) {
            assertThat(lock.isLocked()).isFalse();
        }
    }
}
