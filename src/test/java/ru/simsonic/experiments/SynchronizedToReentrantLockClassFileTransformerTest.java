package ru.simsonic.experiments;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.simsonic.experiments.object.MyClass;
import ru.simsonic.experiments.object.MyInterface;
import ru.simsonic.experiments.utils.TestClassLoader;
import ru.simsonic.experiments.utils.TestClassTransformer;

import static org.assertj.core.api.Assertions.assertThat;

class SynchronizedToReentrantLockClassFileTransformerTest {

    @Test
    void testTransformedClass() throws Exception {
        byte[] transformedBytes = TestClassTransformer.transformClass(MyClass.class);
        TestClassLoader testClassLoader = new TestClassLoader();
        Class<?> transformedClass = testClassLoader.defineClass(MyClass.class.getName(), transformedBytes);

        MyInterface instance = (MyInterface) transformedClass.getDeclaredConstructor().newInstance();

        try {
            instance.testingMethod();
            Assertions.fail();
        } catch (IllegalCallerException expectedException) {
            String message = expectedException.getMessage();
            Assertions.assertEquals(message, "Value of X = 0");
        } catch (Exception ex) {
            Assertions.fail();
        }

        System.gc();
        var debugMap = SharedReentrantLock.getDebugMap();
        int size = debugMap.size();
        System.out.println("size = " + size);

        for (var lock : debugMap.values()) {
            assertThat(lock.isLocked()).isFalse();
        }
    }
}
