package dev.cloud.api.node;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

/**
 * Selects the most suitable node for starting a new service.
 * Uses a least-memory-used strategy by default.
 */
public class NodeSelector {

    /**
     * Selects the best available node that can host a service requiring the given memory.
     * Prefers the node with the most free RAM among all eligible nodes.
     *
     * @param nodes            all known nodes
     * @param requiredMemoryMb the RAM required by the service to be started
     * @return the best matching node, or empty if no eligible node exists
     */
    public Optional<CloudNode> select(Collection<CloudNode> nodes, int requiredMemoryMb) {
        return nodes.stream()
                .filter(node -> node.canHost(requiredMemoryMb))
                .max(Comparator.comparingInt(CloudNode::getAvailableMemory));
    }
}