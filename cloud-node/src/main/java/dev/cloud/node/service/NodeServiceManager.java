package dev.cloud.node.service;

import dev.cloud.api.group.ServiceGroup;
import dev.cloud.api.service.ServiceState;
import dev.cloud.networking.service.ServiceRpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * High-level service lifecycle manager for the node.
 * Delegates creation to {@link NodeServiceFactory} and tracking to {@link NodeServiceTracker}.
 */
public class NodeServiceManager {

    private static final Logger log = LoggerFactory.getLogger(NodeServiceManager.class);

    private final NodeServiceTracker tracker;
    private final NodeServiceFactory factory;
    private final ServiceRpcClient serviceRpcClient;

    public NodeServiceManager(NodeServiceTracker tracker,
                              NodeServiceFactory factory,
                              ServiceRpcClient serviceRpcClient) {
        this.tracker = tracker;
        this.factory = factory;
        this.serviceRpcClient = serviceRpcClient;
    }

    /**
     * Starts a new service for the given group and registers it with the tracker.
     *
     * @param serviceName the unique service name
     * @param group       the group configuration
     */
    public void start(String serviceName, ServiceGroup group) {
        if (tracker.isRunning(serviceName)) {
            log.warn("Service '{}' is already running — ignoring start request.", serviceName);
            return;
        }
        try {
            RunningService service = factory.create(serviceName, group);
            tracker.register(service);
        } catch (Exception e) {
            log.error("Failed to start service '{}': {}", serviceName, e.getMessage(), e);
            serviceRpcClient.reportStateChange(null, serviceName, ServiceState.STOPPED);
        }
    }

    /**
     * Stops a running service, cleans up its resources and reports to master.
     *
     * @param serviceName the name of the service to stop
     * @param force       if {@code true}, kills the container immediately
     */
    public void stop(String serviceName, boolean force) {
        tracker.unregister(serviceName).ifPresentOrElse(service -> {
            service.closeLog();
            try {
                factory.dockerService().stop(
                        serviceName, service.containerId(), service.port(), force);
                factory.directoryManager().delete(serviceName);
                serviceRpcClient.reportStateChange(
                        service.cloudService().getId().toString(),
                        serviceName, ServiceState.STOPPED);
                log.info("Service '{}' stopped.", serviceName);
            } catch (Exception e) {
                log.error("Error stopping service '{}': {}", serviceName, e.getMessage(), e);
            }
        }, () -> log.warn("Stop requested for unknown service '{}'.", serviceName));
    }

    /**
     * Stops all currently running services.
     * Called during node shutdown.
     */
    public void stopAll() {
        log.info("Stopping all {} running services...", tracker.runningCount());
        tracker.all().forEach(s -> stop(s.cloudService().getName(), true));
    }

    /**
     * Returns all currently running services.
     */
    public Collection<RunningService> running() {
        return tracker.all();
    }
}