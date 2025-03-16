package ru.simsonic.experiments.tests;

import org.junit.jupiter.api.Test;
import ru.simsonic.experiments.BaseSynchronizedToReentrantLockClassFileTransformerTest;
import ru.simsonic.experiments.object.MyClass;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CodeGenerationTest extends BaseSynchronizedToReentrantLockClassFileTransformerTest {

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
}
