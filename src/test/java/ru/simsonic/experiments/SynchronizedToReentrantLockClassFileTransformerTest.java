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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SynchronizedToReentrantLockClassFileTransformerTest {

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

        for (var lock : debugMap.values()) {
            assertThat(lock.isLocked()).isFalse();
        }
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

    @Description("Old test")
    @Test
    void testTransformedClass() {
        try {
            myClassModifiedInstance.testingMethod();
            Assertions.fail();
        } catch (IllegalCallerException expectedException) {
            String message = expectedException.getMessage();
            Assertions.assertEquals(message, "Value of X = 0");
        } catch (Exception ex) {
            Assertions.fail();
        }
    }
}
