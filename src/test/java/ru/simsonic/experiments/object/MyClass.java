package ru.simsonic.experiments.object;

import java.io.IOException;

@SuppressWarnings({"FieldMayBeFinal", "SynchronizeOnNonFinalField"})
public class MyClass implements MyInterface {

    private static Object lock1 = new Object();
    private final Object lock2 = "string-type-lock";

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
        synchronized (lock1) {
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
        synchronized (lock1) {
            System.out.println("synchronizedMethodAndBlockOnObject");
        }
    }

    @Override
    public synchronized void testingMethod() {
        try {
            for (int i = 0; i < 10; i += 1) {
                synchronized (lock1) {
                    try {
                        this.ioMethod();
                    } catch (IOException ex) {
                        // Проглотили.
                    }
                    synchronized (this) {
                        synchronized (lock2) {
                            System.out.println("simpleMethodWithBlock");
                            privateMethod();
                        }
                    }
                }
            }
        } catch (IllegalArgumentException ex) {
            synchronized (lock1) {
                System.out.println("simpleMethodWithBlock " + ex.getMessage());
                throw new IllegalCallerException("Value of X = 0", ex);
            }
        } finally {
            synchronized (this) {
                System.out.println("simpleMethodWithBlock FINALLY");
            }
        }
    }

    private void ioMethod() throws IOException {
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
                synchronized (lock1) {
                    synchronized (this) {
                        synchronized (lock2) {
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
