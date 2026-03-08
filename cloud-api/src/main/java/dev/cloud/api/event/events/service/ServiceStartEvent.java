package dev.cloud.api.event.events.service;
import dev.cloud.api.event.CloudEvent;
import dev.cloud.api.event.Cancellable;
import dev.cloud.api.group.ServiceGroup;
import dev.cloud.api.node.CloudNode;

/** Fired before a new service is started. Can be cancelled to prevent the start. */
public class ServiceStartEvent extends CloudEvent implements Cancellable {
    private final ServiceGroup group;
    private final CloudNode targetNode;
    private boolean cancelled;

    public ServiceStartEvent(ServiceGroup group, CloudNode targetNode) {
        this.group = group;
        this.targetNode = targetNode;
    }

    public ServiceGroup getGroup()    { return group; }
    public CloudNode getTargetNode()  { return targetNode; }

    @Override public boolean isCancelled()           { return cancelled; }
    @Override public void setCancelled(boolean c)    { this.cancelled = c; }
}