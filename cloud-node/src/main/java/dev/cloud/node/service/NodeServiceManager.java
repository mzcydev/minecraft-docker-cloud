package dev.cloud.node.service;

import dev.cloud.api.group.ServiceGroup;
import dev.cloud.api.service.ServiceState;
import dev.cloud.networking.service.ServiceReporterClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class NodeServiceManager {

    private static final Logger log = LoggerFactory.getLogger(NodeServiceManager.class);

    private final NodeServiceTracker tracker;
    private final NodeServiceFactory factory;
    private final ServiceReporterClient reporterClient;

    public NodeServiceManager(NodeServiceTracker tracker,
                              NodeServiceFactory factory,
                              ServiceReporterClient reporterClient) {
        this.tracker        = tracker;
        this.factory        = factory;
        this.reporterClient = reporterClient;
    }

    /** Starts a new service and returns the Docker container ID. */
    public String start(String serviceName, ServiceGroup group) {
        if (tracker.isRunning(serviceName)) {
            log.warn("Service '{}' already running.", serviceName);
            return null;
        }
        try {
            RunningService service = factory.create(serviceName, group);
            tracker.register(service);
            return service.containerId();
        } catch (Exception e) {
            log.error("Failed to start '{}': {}", serviceName, e.getMessage(), e);
            reporterClient.reportStateChange(serviceName, ServiceState.STOPPED);
            return null;
        }
    }

    public void stop(String serviceName, boolean force) {
        tracker.unregister(serviceName).ifPresentOrElse(service -> {
            service.closeLog();
            try {
                factory.dockerService().stop(serviceName, service.containerId(), service.port(), force);
                factory.directoryManager().delete(serviceName);
                reporterClient.reportStateChange(serviceName, ServiceState.STOPPED);
                log.info("Service '{}' stopped.", serviceName);
            } catch (Exception e) {
                log.error("Error stopping '{}': {}", serviceName, e.getMessage(), e);
            }
        }, () -> log.warn("Stop requested for unknown service '{}'.", serviceName));
    }

    public void stopAll() {
        log.info("Stopping all {} running services...", tracker.runningCount());
        tracker.all().forEach(s -> stop(s.cloudService().getName(), true));
    }

    public Collection<RunningService> running() {
        return tracker.all();
    }
}
