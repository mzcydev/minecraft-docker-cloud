package dev.cloud.api.event.events.service;

import dev.cloud.api.event.CloudEvent;
import dev.cloud.api.service.CloudService;

/**
 * Fired when a service's runtime data changes, e.g. player count updates.
 */
public class ServiceUpdateEvent extends CloudEvent {
    private final CloudService service;

    public ServiceUpdateEvent(CloudService service) {
        this.service = service;
    }

    public CloudService getService() {
        return service;
    }
}