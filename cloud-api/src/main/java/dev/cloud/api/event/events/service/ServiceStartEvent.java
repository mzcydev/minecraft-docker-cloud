package dev.cloud.api.event.events.service;

import dev.cloud.api.event.Cancellable;
import dev.cloud.api.event.CloudEvent;
import dev.cloud.api.service.CloudService;

public class ServiceStartEvent extends CloudEvent implements Cancellable {
    private final CloudService service;
    private boolean cancelled;

    public ServiceStartEvent(CloudService service) { this.service = service; }
    public CloudService getService() { return service; }

    @Override public boolean isCancelled()       { return cancelled; }
    @Override public void setCancelled(boolean c) { this.cancelled = c; }
}
