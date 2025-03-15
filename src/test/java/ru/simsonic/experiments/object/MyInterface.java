package ru.simsonic.experiments.object;

public interface MyInterface {

    void nonSynchronizedMethod();

    void nonSynchronizedThrowingMethod();

    void nonSynchronizedMethodWithVariable();

    void synchronizedMethod();

    void synchronizedMethodWithVariable();

    void synchronizedThrowingMethod();

    void synchronizedBlockOnThis();

    void synchronizedBlockOnObject();

    void synchronizedMethodAndBlockOnThis();

    void synchronizedMethodAndBlockOnObject();

    void testingMethod();

    void testingMethod_FREEZE();
}
