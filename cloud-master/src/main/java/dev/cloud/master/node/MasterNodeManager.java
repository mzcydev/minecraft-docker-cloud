package dev.cloud.master.node;

import dev.cloud.api.event.EventBus;
import dev.cloud.api.event.events.NodeConnectEvent;
import dev.cloud.api.event.events.NodeDisconnectEvent;
import dev.cloud.api.node.CloudNode;
import dev.cloud.api.node.CloudNodeImpl;
import dev.cloud.api.node.NodeState;
import dev.cloud.networking.GrpcChannelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all nodes connected to the master.
 * Nodes register via gRPC and are tracked here for the lifetime of their connection.
 */
public class MasterNodeManager {

    private static final Logger log = LoggerFactory.getLogger(MasterNodeManager.class);

    /** nodeName → node */
    private final Map<String, CloudNodeImpl> nodes = new ConcurrentHashMap<>();

    private final EventBus eventBus;
    private final GrpcChannelManager channelManager;

    public MasterNodeManager(EventBus eventBus, GrpcChannelManager channelManager) {
        this.eventBus       = eventBus;
        this.channelManager = channelManager;
    }

    /**
     * Registers a newly connected node.
     * Called by the gRPC {@code NodeRpcService} when a node sends a Register RPC.
     *
     * @param node the node to register
     */
    public void register(CloudNodeImpl node) {
        nodes.put(node.getName(), node);
        node.setState(NodeState.CONNECTED);
        eventBus.publish(new NodeConnectEvent(node));
        log.info("Node connected: {} ({})", node.getName(), node.getHost());
    }

    /**
     * Marks a node as disconnected and removes it from the active pool.
     *
     * @param nodeName the name of the disconnecting node
     */
    public void unregister(String nodeName) {
        CloudNodeImpl node = nodes.remove(nodeName);
        if (node != null) {
            node.setState(NodeState.DISCONNECTED);
            channelManager.removeChannel(nodeName);
            eventBus.publish(new NodeDisconnectEvent(node));
            log.info("Node disconnected: {}", nodeName);
        }
    }

    /**
     * Returns the node with the most available memory.
     * Used by the service manager when deciding where to start a service.
     *
     * @return the best node, or empty if no nodes are connected
     */
    public Optional<CloudNode> selectBestNode() {
        return nodes.values().stream()
                .filter(n -> n.getState() == NodeState.CONNECTED)
                .filter(n -> n.getRunningServices() < n.getMaxServices())
                .max((a, b) -> {
                    long freeA = a.getMaxMemoryMb() - a.getUsedMemoryMb();
                    long freeB = b.getMaxMemoryMb() - b.getUsedMemoryMb();
                    return Long.compare(freeA, freeB);
                })
                .map(n -> (CloudNode) n);
    }

    public Optional<CloudNodeImpl> findNode(String name) {
        return Optional.ofNullable(nodes.get(name));
    }

    public Collection<CloudNodeImpl> allNodes() {
        return nodes.values();
    }

    public int connectedCount() {
        return nodes.size();
    }
}