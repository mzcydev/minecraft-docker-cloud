package dev.cloud.api.event.events.group;
import dev.cloud.api.event.CloudEvent;
import dev.cloud.api.group.ServiceGroup;

/** Fired when a service group is deleted. */
public class GroupDeleteEvent extends CloudEvent {
    private final ServiceGroup group;
    public GroupDeleteEvent(ServiceGroup group) { this.group = group; }
    public ServiceGroup getGroup() { return group; }
}