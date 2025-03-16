package ru.simsonic.experiments;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class SharedReentrantLock extends ReentrantLock {

    private static final Map<KeyWrapper, ReentrantLock> OBJECT_TO_REENTRANT_LOCK_MAP = new ConcurrentHashMap<>();

    private static class KeyWrapper {

        private final Object key;
        private final int hashCode;

        private KeyWrapper(Object key) {
            this.key = key;
            this.hashCode = System.identityHashCode(key);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj || obj instanceof KeyWrapper other && this.key == other.key;
        }
    }

    private final KeyWrapper originalMonitoredObjectKey;

    private SharedReentrantLock(KeyWrapper originalMonitoredObjectKey) {
        super(false);
        this.originalMonitoredObjectKey = originalMonitoredObjectKey;
        // System.out.println("SharedReentrantLock init().");
    }

    public static Lock lock(Object object) {
        // System.out.printf("[%s] Locking on %s.%n", Thread.currentThread().getName(), object);
        Lock lock = OBJECT_TO_REENTRANT_LOCK_MAP.computeIfAbsent(
                new KeyWrapper(object),
                k -> {
                    ReentrantLock result = new SharedReentrantLock(k);
                    result.lock();
                    return result;
                }
        );

        System.out.printf("[%s] Lock acquired on %s.%n", Thread.currentThread().getName(), object);
        return lock;
    }

    public static Map<Object, ReentrantLock> getDebugMap() {
        System.out.println("getDebugMap");
        return Map.copyOf(OBJECT_TO_REENTRANT_LOCK_MAP);
    }

    @Override
    public void unlock() {
        System.out.printf("[%s] Unlocking on %s...%n", Thread.currentThread().getName(), originalMonitoredObjectKey.key);
        Lock currentState = OBJECT_TO_REENTRANT_LOCK_MAP.computeIfPresent(
                originalMonitoredObjectKey,
                SharedReentrantLock::tryRemove
        );
        if (currentState == null) {
            System.out.printf("[%s] Removing lock on %s from map.%n", Thread.currentThread().getName(), originalMonitoredObjectKey.key);
        }
        super.unlock();
    }

    private static ReentrantLock tryRemove(KeyWrapper object, ReentrantLock lock) {
        int holdCount = lock.getHoldCount();
        if (holdCount > 1) {
            return lock;
        }

        return null;
    }
}
