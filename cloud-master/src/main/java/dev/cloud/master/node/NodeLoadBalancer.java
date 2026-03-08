package dev.cloud.master.node;

import dev.cloud.api.group.ServiceGroup;
import dev.cloud.api.node.CloudNodeImpl;
import dev.cloud.api.node.NodeState;

import java.util.Comparator;
import java.util.Optional;

/**
 * Selects the most suitable node for starting a new service.
 * Strategy: pick the node with the most free memory that still has service capacity.
 */
public class NodeLoadBalancer {

    private final NodeRegistry registry;

    public NodeLoadBalancer(NodeRegistry registry) {
        this.registry = registry;
    }

    /**
     * Selects the best node for the given group's memory requirement.
     *
     * @param group the group whose service needs a node
     * @return the selected node, or empty if none is eligible
     */
    public Optional<CloudNodeImpl> selectFor(ServiceGroup group) {
        return registry.allNodes().stream()
                .filter(n -> n.getState() == NodeState.CONNECTED)
                .filter(n -> n.getRunningServices() < n.getMaxServices())
                .filter(n -> (n.getMaxMemoryMb() - n.getUsedMemoryMb()) >= group.getMemory())
                .max(Comparator.comparingLong(n -> n.getMaxMemoryMb() - n.getUsedMemoryMb()));
    }
}