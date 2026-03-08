package dev.cloud.api.event.events.node;

import dev.cloud.api.event.CloudEvent;
import dev.cloud.api.node.CloudNode;
import dev.cloud.api.node.NodeInfo;

/**
 * Fired when the master receives a heartbeat and updates a node's resource info.
 */
public class NodeInfoUpdateEvent extends CloudEvent {
    private final CloudNode node;
    private final NodeInfo info;

    public NodeInfoUpdateEvent(CloudNode node, NodeInfo info) {
        this.node = node;
        this.info = info;
    }

    public CloudNode getNode() {
        return node;
    }

    public NodeInfo getInfo() {
        return info;
    }
}