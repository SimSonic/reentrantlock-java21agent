package ru.simsonic.experiments.object;

public interface MyInterface {

    void invokeStaticMethods();

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

    void synchronizedThrowingMethodWithBlocks();

    void synchronizedConsumer(Runnable runnable);

    void oldTestingMethod();

    void testingMethod_FREEZE();
}
