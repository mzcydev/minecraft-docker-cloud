package dev.cloud.api.event.events.group;
import dev.cloud.api.event.CloudEvent;
import dev.cloud.api.group.ServiceGroup;

/** Fired when a new service group is created. */
public class GroupCreateEvent extends CloudEvent {
    private final ServiceGroup group;
    public GroupCreateEvent(ServiceGroup group) { this.group = group; }
    public ServiceGroup getGroup() { return group; }
}