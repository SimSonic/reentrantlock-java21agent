package ru.simsonic.experiments;

import jdk.jfr.Description;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.simsonic.experiments.object.MyClass;
import ru.simsonic.experiments.object.MyInterface;
import ru.simsonic.experiments.utils.TestClassLoader;
import ru.simsonic.experiments.utils.TestClassTransformer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SynchronizedToReentrantLockClassFileTransformerTest {

    private static final int VIRTUAL_THREADS_NUM = 1; // АГА!!! На 2 и выше - виснет!
    private static final int VIRTUAL_THREAD_DEPTH = 10;
    private static final int VIRTUAL_THREAD_ITERS = 10;

    private static MyInterface myClassModifiedInstance;

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

    @Test
    void testGetClass() {
        Class<?> transformedClass = myClassModifiedInstance.getClass();
        assertThat(transformedClass.getName())
                .isEqualTo(MyClass.class.getName());

        // FAILS:
        // assertThat(myClassModifiedInstance).isInstanceOf(MyClass.class);
    }

    @Test
    void testNonSynchronizedMethods() {
        myClassModifiedInstance.nonSynchronizedMethod();
        myClassModifiedInstance.nonSynchronizedMethodWithVariable();
    }

    @Test
    void testNonSynchronizedThrowingMethod() {
        assertThatThrownBy(
                () -> myClassModifiedInstance.nonSynchronizedThrowingMethod()
        );
    }

    @Test
    void testStaticSynchronizedMethods() {
        myClassModifiedInstance.invokeStaticMethods();
    }

    @Test
    void testSynchronizedMethods() {
        myClassModifiedInstance.synchronizedMethod();
        myClassModifiedInstance.synchronizedMethodWithVariable();
    }

    @Test
    void testSynchronizedBlocks() {
        myClassModifiedInstance.synchronizedBlockOnThis();
        myClassModifiedInstance.synchronizedMethodAndBlockOnThis();
        myClassModifiedInstance.synchronizedBlockOnObject();
        myClassModifiedInstance.synchronizedMethodAndBlockOnObject();
    }

    @Test
    void testSynchronizedThrowingMethod() {
        assertThatThrownBy(
                () -> myClassModifiedInstance.synchronizedThrowingMethod()
        );
    }

    @Test
    void testDeeplySynchronizedThrowingMethod() {
        assertThatThrownBy(
                () -> myClassModifiedInstance.synchronizedThrowingMethodWithBlocks()
        );
    }

    @Test
    void tryLoad() {
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            AtomicReference<Runnable> runnableRef = new AtomicReference<>(
                    () -> myClassModifiedInstance.nonSynchronizedThrowingMethod()
            );
            for (int virtualDepthId = 0; virtualDepthId < VIRTUAL_THREAD_DEPTH; virtualDepthId += 1) {
                Runnable innerRunnable = runnableRef.get();
                runnableRef.set(
                        () -> myClassModifiedInstance.synchronizedConsumer(innerRunnable)
                );
            }

            for (int virtualThreadId = 0; virtualThreadId < VIRTUAL_THREADS_NUM; virtualThreadId += 1) {
                executorService.submit(() -> {
                    for (int iterationId = 0; iterationId < VIRTUAL_THREAD_ITERS; iterationId += 1) {
                        assertThatThrownBy(
                                () -> myClassModifiedInstance.synchronizedConsumer(runnableRef.get())
                        );
                    }
                });
            }
        }
    }

    @Description("Old test")
    @Test
    void testTransformedClass() {
        try {
            myClassModifiedInstance.oldTestingMethod();
            Assertions.fail();
        } catch (IllegalCallerException expectedException) {
            String message = expectedException.getMessage();
            Assertions.assertEquals(message, "Value of X = 0");
        } catch (Exception ex) {
            Assertions.fail();
        }
    }
}
