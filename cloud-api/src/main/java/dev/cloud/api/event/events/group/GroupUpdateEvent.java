package dev.cloud.api.event.events.group;
import dev.cloud.api.event.CloudEvent;
import dev.cloud.api.group.ServiceGroup;

/** Fired when an existing service group's configuration is updated. */
public class GroupUpdateEvent extends CloudEvent {
    private final ServiceGroup group;
    public GroupUpdateEvent(ServiceGroup group) { this.group = group; }
    public ServiceGroup getGroup() { return group; }
}