package dev.cloud.api.event.events.node;

import dev.cloud.api.event.CloudEvent;
import dev.cloud.api.node.CloudNode;

/**
 * Fired when a node successfully connects and registers with the master.
 */
public class NodeConnectEvent extends CloudEvent {
    private final CloudNode node;

    public NodeConnectEvent(CloudNode node) {
        this.node = node;
    }

    public CloudNode getNode() {
        return node;
    }
}