package dev.cloud.master.node;

import dev.cloud.api.event.EventBus;
import dev.cloud.api.event.events.node.NodeConnectEvent;
import dev.cloud.api.event.events.node.NodeDisconnectEvent;
import dev.cloud.api.node.CloudNode;
import dev.cloud.api.node.CloudNodeImpl;
import dev.cloud.api.node.NodeInfo;
import dev.cloud.api.node.NodeManager;
import dev.cloud.api.node.NodeState;
import dev.cloud.networking.GrpcChannelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class MasterNodeManager implements NodeManager {

    private static final Logger log = LoggerFactory.getLogger(MasterNodeManager.class);

    private final Map<String, CloudNodeImpl> nodes = new ConcurrentHashMap<>();
    private final EventBus eventBus;
    private final GrpcChannelManager channelManager;

    public MasterNodeManager(EventBus eventBus, GrpcChannelManager channelManager) {
        this.eventBus       = eventBus;
        this.channelManager = channelManager;
    }

    @Override
    public void registerNode(CloudNode node) {
        CloudNodeImpl impl = (CloudNodeImpl) node;
        nodes.put(impl.getName(), impl);
        impl.setState(NodeState.CONNECTED);
        eventBus.publish(new NodeConnectEvent(impl));
        log.info("Node connected: {} ({})", impl.getName(), impl.getHost());
    }

    @Override
    public void unregisterNode(String name) {
        CloudNodeImpl node = nodes.remove(name);
        if (node != null) {
            node.setState(NodeState.DISCONNECTED);
            channelManager.closeChannel(name);
            eventBus.publish(new NodeDisconnectEvent(node, "unregistered"));
            log.info("Node disconnected: {}", name);
        }
    }

    @Override
    public Optional<CloudNode> getNode(String name) {
        return Optional.ofNullable(nodes.get(name));
    }

    @Override
    public Collection<CloudNode> getAllNodes() {
        return List.copyOf(nodes.values());
    }

    @Override
    public Collection<CloudNode> getAvailableNodes() {
        return nodes.values().stream()
                .filter(n -> n.getState() == NodeState.CONNECTED)
                .map(n -> (CloudNode) n)
                .toList();
    }

    @Override
    public void updateNodeInfo(NodeInfo info) {
        CloudNodeImpl node = nodes.get(info.nodeName());
        if (node != null) {
            node.applyInfo(info);
            node.setLastHeartbeat(Instant.now());
        }
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

    public Optional<CloudNode> selectBestNode(int requiredMemoryMb) {
        return nodes.values().stream()
                .filter(n -> n.getState() == NodeState.CONNECTED)
                .filter(n -> n.getRunningServices() < n.getMaxServices())
                .filter(n -> n.getAvailableMemoryMb() >= requiredMemoryMb)
                .max((a, b) -> Long.compare(
                        a.getMaxMemoryMb() - a.getUsedMemoryMb(),
                        b.getMaxMemoryMb() - b.getUsedMemoryMb()))
                .map(n -> (CloudNode) n);
    }
}
