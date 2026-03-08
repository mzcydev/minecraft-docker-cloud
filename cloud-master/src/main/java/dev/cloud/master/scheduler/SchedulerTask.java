package dev.cloud.master.scheduler;

/**
 * Contract for tasks that can be scheduled by the {@link TaskExecutor}.
 */
public interface SchedulerTask extends Runnable {

    /**
     * Returns the name of this task for logging purposes.
     */
    String getTaskName();
}