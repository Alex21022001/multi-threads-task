package com.alexsitiy.task;

import com.alexsitiy.util.TimeWaiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class DebouncerImpl implements Debouncer<Runnable> {

    private final Logger logger = LoggerFactory.getLogger(DebouncerImpl.class);

    private final Set<Runnable> usedTask = ConcurrentHashMap.newKeySet();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final Long interval;

    public DebouncerImpl(Long interval) {
        this.interval = interval;
    }

    @Override
    public void call(Runnable task) {
        if (!usedTask.contains(task)) {

            Runnable modifiedTask = () -> {
                // Run the task immediately and measure its execution time. If it's bigger than 1S - doesn't wait, if not - wait the rest of the 1S.
                long start = System.currentTimeMillis();
                task.run();
                long finish = System.currentTimeMillis();
                long executionTime = finish - start;

                logger.debug("execution time is {}", executionTime);

                if (executionTime < interval) {
                    TimeWaiter.waitTime(interval - executionTime);
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



}
