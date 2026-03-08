package dev.cloud.api.event.events.node;
import dev.cloud.api.event.CloudEvent;
import dev.cloud.api.node.CloudNode;

/** Fired when a node disconnects from the master, either gracefully or due to a timeout. */
public class NodeDisconnectEvent extends CloudEvent {
    private final CloudNode node;
    private final String reason;

    public NodeDisconnectEvent(CloudNode node, String reason) {
        this.node = node;
        this.reason = reason;
    }

    public CloudNode getNode()  { return node; }
    public String getReason()   { return reason; }
}