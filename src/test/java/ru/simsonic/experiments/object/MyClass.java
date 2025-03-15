package ru.simsonic.experiments.object;

import java.io.IOException;

@SuppressWarnings({"FieldMayBeFinal", "SynchronizeOnNonFinalField"})
public class MyClass implements MyInterface {

    private static Object staticLock = new Object();
    private final Object fieldLock = "string-type-lock-is-just-for-fun";

    public static synchronized void staticSynchronizedMethod() {
        System.out.println("staticSynchronizedMethod");
    }

    public static void staticSynchronizedBlock() {
        synchronized (staticLock) {
            System.out.println("staticSynchronizedBlock");
        }
    }

    @Override
    public void invokeStaticMethods() {
        MyClass.staticSynchronizedMethod();
        MyClass.staticSynchronizedBlock();
    }

    @Override
    public void nonSynchronizedMethod() {
        System.out.println("nonSynchronizedMethod");
    }

    @Override
    public void nonSynchronizedThrowingMethod() {
        System.out.println("nonSynchronizedThrowingMethod");
        throw new RuntimeException("nonSynchronizedThrowingMethod");
    }

    @Override
    public void nonSynchronizedMethodWithVariable() {
        String variable = "nonSynchronizedMethodWithVariables";
        System.out.println(variable);
    }

    @Override
    public synchronized void synchronizedMethod() {
        System.out.println("synchronizedMethod");
    }

    @Override
    public synchronized void synchronizedMethodWithVariable() {
        String variable = "synchronizedMethodWithVariable";
        System.out.println(variable);
    }

    @Override
    public synchronized void synchronizedThrowingMethod() {
        throw new IllegalArgumentException("synchronizedThrowingMethod");
    }

    @Override
    public void synchronizedBlockOnThis() {
        synchronized (this) {
            System.out.println("synchronizedBlockOnThis");
        }
    }

    @Override
    public void synchronizedBlockOnObject() {
        synchronized (staticLock) {
            System.out.println("synchronizedBlockOnObject");
        }
    }

    @Override
    public synchronized void synchronizedMethodAndBlockOnThis() {
        synchronized (this) {
            System.out.println("synchronizedMethodAndBlockOnThis");
        }
    }

    @Override
    public synchronized void synchronizedMethodAndBlockOnObject() {
        synchronized (staticLock) {
            System.out.println("synchronizedMethodAndBlockOnObject");
        }
    }

    @Override
    public synchronized void synchronizedThrowingMethodWithBlocks() {
        synchronized (this) {
            synchronized (staticLock) {
                throw new IllegalArgumentException("synchronizedThrowingMethod");
            }
        }
    }

    @Override
    public synchronized void synchronizedConsumer(Runnable runnable) {
        runnable.run();
    }

    // Дальше древний код!

    @Override
    public synchronized void oldTestingMethod() {
        try {
            for (int i = 0; i < 10; i += 1) {
                synchronized (staticLock) {
                    try {
                        this.privateNonSynchronizedThrowingMethod();
                    } catch (IOException ex) {
                        // Проглотили.
                    }
                    synchronized (this) {
                        synchronized (fieldLock) {
                            System.out.println("simpleMethodWithBlock");
                            privateMethod();
                        }
                    }
                }
            }
        } catch (IllegalArgumentException ex) {
            synchronized (staticLock) {
                System.out.println("simpleMethodWithBlock " + ex.getMessage());
                throw new IllegalCallerException("Value of X = 0", ex);
            }
        } finally {
            synchronized (this) {
                System.out.println("simpleMethodWithBlock FINALLY");
            }
        }
    }

    private void privateNonSynchronizedThrowingMethod() throws IOException {
        throw new IOException("I/O is bad...");
    }

    private synchronized void privateMethod() {
        System.out.println("ABC go away from privateMethod()!");
        privateStaticMethod(this);
    }

    private static void privateStaticMethod(Object targetLock) {
        synchronized (targetLock) {
            System.out.println("ABC go away from privateStaticMethod()!");
            throw new IllegalArgumentException("ABC");
        }
    }

    @Override
    public synchronized void testingMethod_FREEZE() {
        try {
            for (int i = 0; i < 10; i += 1) {
                synchronized (staticLock) {
                    synchronized (this) {
                        synchronized (fieldLock) {
                            System.out.println("simpleMethodWithBlock");
                            throw new IllegalArgumentException("abc");
                        }
                    }
                }
            }
        } catch (IllegalArgumentException ex) {
            // synchronized (lock1)
            {
                System.out.println("simpleMethodWithBlock " + ex.getMessage());
            }
        } finally {
            synchronized (this) {
                System.out.println("simpleMethodWithBlock FINALLY");
            }
        }
    }
}
