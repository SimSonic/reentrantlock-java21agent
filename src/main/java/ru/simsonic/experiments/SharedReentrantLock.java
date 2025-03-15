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
        // System.out.println("lock init()!");
    }

    public static Lock lock(Object object) {
        // System.out.println("static lock!");
        return OBJECT_TO_REENTRANT_LOCK_MAP.compute(
                object,
                (k, v) -> {
                    ReentrantLock result = v != null ? v : new SharedReentrantLock(k);
                    result.lock();
                    return result;
                }
        );
    }

    public static Map<Object, ReentrantLock> getDebugMap() {
        // System.out.println("getDebugMap");
        return Map.copyOf(OBJECT_TO_REENTRANT_LOCK_MAP);
    }

    @Override
    public void unlock() {
        // System.out.println("unlock");
        OBJECT_TO_REENTRANT_LOCK_MAP.computeIfPresent(
                originalMonitoredObject,
                SharedReentrantLock::tryRemove
        );
        super.unlock();
    }

    private static ReentrantLock tryRemove(Object object, ReentrantLock lock) {
        int holdCount = lock.getHoldCount();
        return holdCount > 1 ? lock : null;
    }
}
