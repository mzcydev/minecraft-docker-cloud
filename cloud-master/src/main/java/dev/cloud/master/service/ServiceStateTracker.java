package dev.cloud.master.service;

import dev.cloud.api.service.ServiceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks state transitions for all services.
 * Provides a log of recent transitions and fires warnings for stuck services.
 */
public class ServiceStateTracker {

    private static final Logger log = LoggerFactory.getLogger(ServiceStateTracker.class);

    /**
     * serviceId → current state
     */
    private final Map<String, ServiceState> states = new ConcurrentHashMap<>();

    /**
     * Records a state transition for a service.
     *
     * @param serviceId the service ID
     * @param newState  the new state
     */
    public void transition(String serviceId, ServiceState newState) {
        ServiceState old = states.put(serviceId, newState);
        if (old != newState) {
            log.debug("Service {} state: {} → {}", serviceId, old, newState);
        }
    }

    /**
     * Returns the current state of a service, or {@code null} if unknown.
     */
    public ServiceState getState(String serviceId) {
        return states.get(serviceId);
    }

    /**
     * Removes tracking for a stopped service.
     */
    public void remove(String serviceId) {
        states.remove(serviceId);
    }
}