package ru.simsonic.experiments.object;

import ru.simsonic.experiments.SharedReentrantLock;

import java.util.ArrayDeque;
import java.util.concurrent.locks.Lock;

public class MyClassAnalogue {

    /*
    private static Object lock1 = new Object();
    private final Object lock2 = new Object();

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
    */

    public void simpleSynchronizedMethod_EXPECTED_NAIVE() {
        String simpleSynchronizedMethod = "simpleSynchronizedMethod";
        var asm_lockStack = new ArrayDeque();
        var lock = SharedReentrantLock.lock(this);
        asm_lockStack.push(lock);
        lock.lock();
        System.out.println(simpleSynchronizedMethod);
        ((Lock) asm_lockStack.pop()).unlock();
    }

    public void simpleSynchronizedMethod_EXPECTED() {
        var __asm_lockStack = new ArrayDeque<Lock>();

        var __ams_lock = SharedReentrantLock.lock(this);
        __asm_lockStack.push(__ams_lock);
        __ams_lock.lock();
        try {
            String simpleSynchronizedMethod = "simpleSynchronizedMethod";
            System.out.println(simpleSynchronizedMethod);
        } finally {
            __asm_lockStack.pop().unlock();
        }
    }

    /*
    public void simpleMethodWithBlock() {
        var ignored = "ignored";
        synchronized (this) {
            System.out.println("simpleMethodWithBlock");
        }
        ignored = "123";
    }
    */

        /*
    @Override
    public synchronized void theMostComplexMethodEver() {
        var x = 1;
        synchronized (lock1) {
            var y = 2;
            try {
                synchronized (lock2) {
                    y -= 2;
                    try {
                        var z = 3;
                        z /= y;
                    } catch (IllegalArgumentException ex) {
                        x = 100;
                    }
                }
            } catch (Exception ex) {
                throw new IllegalCallerException("Value of X = " + x);
            } finally {
                synchronized (lock2) {
                    synchronized (lock1) {
                        x = y;
                    }
                }
            }
    }
    }
    */
}
