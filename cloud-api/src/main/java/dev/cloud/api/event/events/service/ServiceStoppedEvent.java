package dev.cloud.api.event.events.service;
import dev.cloud.api.event.CloudEvent;
import dev.cloud.api.service.CloudService;

/** Fired after a service has fully stopped and its container has been removed. */
public class ServiceStoppedEvent extends CloudEvent {
    private final CloudService service;

    public ServiceStoppedEvent(CloudService service) { this.service = service; }
    public CloudService getService() { return service; }
}