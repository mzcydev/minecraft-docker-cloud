package dev.cloud.api.event.events.service;

import dev.cloud.api.event.CloudEvent;
import dev.cloud.api.service.CloudService;
import dev.cloud.api.service.ServiceState;

/**
 * Fired whenever a service transitions from one {@link ServiceState} to another.
 */
public class ServiceStateChangeEvent extends CloudEvent {
    private final CloudService service;
    private final ServiceState oldState;
    private final ServiceState newState;

    public ServiceStateChangeEvent(CloudService service, ServiceState oldState, ServiceState newState) {
        this.service = service;
        this.oldState = oldState;
        this.newState = newState;
    }

    public CloudService getService() {
        return service;
    }

    public ServiceState getOldState() {
        return oldState;
    }

    public ServiceState getNewState() {
        return newState;
    }
}