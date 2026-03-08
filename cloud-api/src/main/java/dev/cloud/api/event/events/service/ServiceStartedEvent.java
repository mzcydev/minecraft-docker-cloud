package dev.cloud.api.event.events.service;
import dev.cloud.api.event.CloudEvent;
import dev.cloud.api.service.CloudService;

/** Fired after a service has fully started and reported itself as {@code ONLINE}. */
public class ServiceStartedEvent extends CloudEvent {
    private final CloudService service;

    public ServiceStartedEvent(CloudService service) { this.service = service; }
    public CloudService getService() { return service; }
}