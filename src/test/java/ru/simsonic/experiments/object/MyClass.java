package ru.simsonic.experiments.object;

import java.io.IOException;

@SuppressWarnings({ "FieldMayBeFinal", "SynchronizeOnNonFinalField" })
public class MyClass implements MyInterface {

    private static Object lock1 = new Object();
    private final Object lock2 = "string-type-lock";

    public void superSimpleMethod() {
        System.out.println("superSimpleMethod");
    }

    public void simpleMethod() {
        String simpleMethod = "simpleMethod";
        System.out.println(simpleMethod);
    }

    public synchronized void superSimpleSynchronizedMethod() {
        System.out.println("superSimpleSynchronizedMethod");
    }

    public synchronized void simpleSynchronizedMethod() {
        String simpleSynchronizedMethod = "simpleSynchronizedMethod";
        System.out.println(simpleSynchronizedMethod);
    }

    public synchronized void testingMethod1() {
        throw new IllegalArgumentException("ABC");
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
