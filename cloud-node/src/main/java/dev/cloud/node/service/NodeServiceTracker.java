package dev.cloud.node.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks all services currently running on this node.
 * Thread-safe — updated by the service manager from gRPC handler threads.
 */
public class NodeServiceTracker {

    private static final Logger log = LoggerFactory.getLogger(NodeServiceTracker.class);

    /**
     * serviceName → running service
     */
    private final Map<String, RunningService> services = new ConcurrentHashMap<>();

    /**
     * Registers a newly started service.
     *
     * @param service the running service to track
     */
    public void register(RunningService service) {
        services.put(service.cloudService().getName(), service);
        log.debug("Tracking service: {}", service.cloudService().getName());
    }

    /**
     * Removes a stopped service from the tracker.
     *
     * @param serviceName the name of the service to untrack
     * @return the removed {@link RunningService}, or empty if not found
     */
    public Optional<RunningService> unregister(String serviceName) {
        RunningService removed = services.remove(serviceName);
        if (removed != null) log.debug("Untracked service: {}", serviceName);
        return Optional.ofNullable(removed);
    }

    /**
     * Finds a running service by name.
     *
     * @param serviceName the service name
     * @return an {@link Optional} containing the service, or empty if not found
     */
    public Optional<RunningService> find(String serviceName) {
        return Optional.ofNullable(services.get(serviceName));
    }

    /**
     * Returns all currently tracked services.
     */
    public Collection<RunningService> all() {
        return services.values();
    }

    /**
     * Returns the number of services currently running on this node.
     */
    public int runningCount() {
        return services.size();
    }

    /**
     * Returns {@code true} if a service with the given name is tracked.
     */
    public boolean isRunning(String serviceName) {
        return services.containsKey(serviceName);
    }
}