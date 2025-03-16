package ru.simsonic.experiments;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class SharedReentrantLock extends ReentrantLock {

    private static final Map<Object, ReentrantLock> OBJECT_TO_REENTRANT_LOCK_MAP = new ConcurrentHashMap<>();

    private final Object originalMonitoredObject;

    private SharedReentrantLock(Object originalMonitoredObject) {
        super(true);
        this.originalMonitoredObject = originalMonitoredObject;
        System.out.println("SharedReentrantLock init().");
    }

    public static Lock lock(Object object) {
        System.out.printf("[%s] Locking on %s.%n", Thread.currentThread().getName(), object);
        return OBJECT_TO_REENTRANT_LOCK_MAP.computeIfAbsent(
                object,
                ignored -> {
                    ReentrantLock result = new SharedReentrantLock(object);
                    result.lock();
                    System.out.printf("[%s] Lock acquired on %s.%n", Thread.currentThread().getName(), object);
                    return result;
                }
        );
    }

    public static Map<Object, ReentrantLock> getDebugMap() {
        System.out.println("getDebugMap");
        return Map.copyOf(OBJECT_TO_REENTRANT_LOCK_MAP);
    }

    @Override
    public void unlock() {
        System.out.printf("[%s] Unlocking on %s...%n", Thread.currentThread().getName(), originalMonitoredObject);
        OBJECT_TO_REENTRANT_LOCK_MAP.computeIfPresent(
                originalMonitoredObject,
                SharedReentrantLock::tryRemove
        );
        super.unlock();
    }

    private static ReentrantLock tryRemove(Object object, ReentrantLock lock) {
        int holdCount = lock.getHoldCount();
        if (holdCount > 1) {
            return lock;
        }

        System.out.printf("[%s] Removing lock on %s from map.%n", Thread.currentThread().getName(), object);
        return null;
    }
}
