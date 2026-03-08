package dev.cloud.master.service;

import dev.cloud.api.group.ServiceGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Queues service start requests when no node is currently available.
 * The {@link ServiceScaler} drains this queue whenever nodes become available.
 */
public class ServiceQueue {

    private static final Logger log = LoggerFactory.getLogger(ServiceQueue.class);

    private final BlockingQueue<ServiceGroup> queue = new LinkedBlockingQueue<>();

    /**
     * Adds a group to the start queue.
     *
     * @param group the group to start a service for
     */
    public void enqueue(ServiceGroup group) {
        queue.offer(group);
        log.debug("Queued service start for group '{}'.", group.getName());
    }

    /**
     * Polls the next pending start request, or returns {@code null} if the queue is empty.
     */
    public ServiceGroup poll() {
        return queue.poll();
    }

    /**
     * Returns {@code true} if there are pending start requests.
     */
    public boolean hasPending() {
        return !queue.isEmpty();
    }

    /**
     * Returns the number of pending start requests.
     */
    public int size() {
        return queue.size();
    }
}