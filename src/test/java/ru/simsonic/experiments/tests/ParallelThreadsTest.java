package ru.simsonic.experiments.tests;

import org.junit.jupiter.api.Test;
import ru.simsonic.experiments.BaseSynchronizedToReentrantLockClassFileTransformerTest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ParallelThreadsTest extends BaseSynchronizedToReentrantLockClassFileTransformerTest {

    private static final int THREADS_NUM = 10;
    private static final int THREAD_DEPTH = 1;
    private static final int THREAD_ITERATIONS = 1;

    @Test
    void trySeveralPlatformThreads() {
        ThreadFactory factory = Thread.ofPlatform().name("p-", 1).factory();
        runThreads(factory);
    }

    @Test
    void trySeveralVirtualThreads() {
        ThreadFactory factory = Thread.ofVirtual().name("v-", 1).factory();
        runThreads(factory);
    }

    private static void runThreads(ThreadFactory factory) {
        try (ExecutorService executorService = Executors.newThreadPerTaskExecutor(factory)) {
            AtomicReference<Runnable> runnableRef = new AtomicReference<>(
                    () -> myClassModifiedInstance.synchronizedThrowingMethodWithBlocks()
            );
            for (int virtualDepthId = 0; virtualDepthId < THREAD_DEPTH; virtualDepthId += 1) {
                Runnable innerRunnable = runnableRef.get();
                runnableRef.set(
                        () -> myClassModifiedInstance.synchronizedConsumer(innerRunnable)
                );
            }

            for (int virtualThreadId = 0; virtualThreadId < THREADS_NUM; virtualThreadId += 1) {
                executorService.submit(() -> {
                    for (int iterationId = 0; iterationId < THREAD_ITERATIONS; iterationId += 1) {
                        assertThatThrownBy(
                                () -> myClassModifiedInstance.synchronizedConsumer(runnableRef.get())
                        );
                    }
                });
            }
        }
    }
}
