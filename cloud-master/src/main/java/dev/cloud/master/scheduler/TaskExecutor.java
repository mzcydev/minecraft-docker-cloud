package dev.cloud.master.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Shared scheduled executor for all repeating master tasks.
 * Centralizes thread management and provides error-safe task wrapping.
 */
public class TaskExecutor {

    private static final Logger log = LoggerFactory.getLogger(TaskExecutor.class);

    private final ScheduledExecutorService executor;

    public TaskExecutor(int threadPoolSize) {
        this.executor = Executors.newScheduledThreadPool(threadPoolSize, r -> {
            Thread t = new Thread(r, "task-executor");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Schedules a task to run repeatedly at a fixed rate.
     *
     * @param task         the task to run
     * @param initialDelay delay before the first execution (seconds)
     * @param period       interval between executions (seconds)
     * @return a {@link ScheduledFuture} that can be used to cancel the task
     */
    public ScheduledFuture<?> scheduleAtFixedRate(SchedulerTask task,
                                                  long initialDelay, long period) {
        return executor.scheduleAtFixedRate(() -> {
            try {
                task.run();
            } catch (Exception e) {
                log.error("Error in scheduled task '{}': {}", task.getTaskName(), e.getMessage(), e);
            }
        }, initialDelay, period, TimeUnit.SECONDS);
    }

    /**
     * Shuts down the executor, cancelling all pending tasks.
     */
    public void shutdown() {
        executor.shutdownNow();
        log.info("TaskExecutor shut down.");
    }
}