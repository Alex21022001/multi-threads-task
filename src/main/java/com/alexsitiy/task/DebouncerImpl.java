package com.alexsitiy.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class DebouncerImpl implements Debouncer<Runnable> {

    private final Logger logger = LoggerFactory.getLogger(DebouncerImpl.class);
    private final Set<Runnable> usedTask = ConcurrentHashMap.newKeySet();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final AtomicLong executionTime = new AtomicLong(0);
    private static final Long ONE_SECOND_IN_MS = 1000L;

    @Override
    public void call(Runnable task) {
        if (!usedTask.contains(task)) {

            Runnable modifiedTask = () -> {
                try {
                    long prevExcTime = executionTime.get();

                    if (prevExcTime >= ONE_SECOND_IN_MS) {
                        runWithTimeMeasurement(task);
                    } else {
                        runWithTimeMeasurement(task);
                        Thread.sleep(ONE_SECOND_IN_MS - prevExcTime);
                    }
                } catch (InterruptedException e) {
                    logger.warn("{} was interrupted during waiting for the next task",Thread.currentThread().getName());
                    throw new RuntimeException(e);
                } finally {
                    usedTask.remove(task);
                }
            };

            executorService.submit(modifiedTask);

            usedTask.add(task);
        }
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
    }

    private void runWithTimeMeasurement(Runnable task) {
        long start = System.currentTimeMillis();
        task.run();
        long finish = System.currentTimeMillis();
        long newExecutionTime = finish - start;

        executionTime.set(newExecutionTime);
        logger.debug("execution time is {}", newExecutionTime);
    }

}
