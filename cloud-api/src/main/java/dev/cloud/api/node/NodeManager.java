package dev.cloud.api.node;

import java.util.Collection;
import java.util.Optional;

/**
 * Manages all nodes registered with the master.
 */
public interface NodeManager {

    /**
     * Registers a newly connected node.
     *
     * @param node the node to register
     */
    void registerNode(CloudNode node);

    /**
     * Unregisters a node, typically called when it disconnects.
     *
     * @param name the name of the node to unregister
     */
    void unregisterNode(String name);

    /**
     * Looks up a node by name.
     *
     * @param name the node name
     * @return an {@link Optional} containing the node, or empty if not found
     */
    Optional<CloudNode> getNode(String name);

    /**
     * Returns all currently registered nodes regardless of state.
     */
    Collection<CloudNode> getAllNodes();

    /**
     * Returns all nodes that are in {@link NodeState#CONNECTED} state
     * and therefore available for scheduling.
     */
    Collection<CloudNode> getAvailableNodes();

    /**
     * Updates a node's runtime info from a received heartbeat.
     *
     * @param info the latest heartbeat payload
     */
    void updateNodeInfo(NodeInfo info);
}