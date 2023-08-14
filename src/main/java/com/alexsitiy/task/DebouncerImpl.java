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
    private static final Long ONE_SECOND_IN_MS = 1000L;

    @Override
    public void call(Runnable task) {
        if (!usedTask.contains(task)) {

            Runnable modifiedTask = () -> {
                // Run the task immediately and measure its execution time. If it's bigger than 1S - doesn't wait, if not - wait the rest of the 1S.
                long executionTime = runTaskWithMeasurement(task);

                if (executionTime < ONE_SECOND_IN_MS) {
                    try {
                        Thread.sleep(ONE_SECOND_IN_MS - executionTime);
                    } catch (InterruptedException e) {
                        logger.warn("{} was interrupted during the Task interval",Thread.currentThread().getName());
                        throw new RuntimeException(e);
                    }
                }

                usedTask.remove(task);
            };

            executorService.submit(modifiedTask);

            usedTask.add(task);
        }
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
    }

    private long runTaskWithMeasurement(Runnable task) {
        long start = System.currentTimeMillis();
        task.run();
        long finish = System.currentTimeMillis();
        long newExecutionTime = finish - start;

        logger.debug("execution time is {}", newExecutionTime);
        return newExecutionTime;
    }


}
