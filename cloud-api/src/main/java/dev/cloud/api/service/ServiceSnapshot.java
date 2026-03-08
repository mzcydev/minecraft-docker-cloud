package dev.cloud.api.service;

import dev.cloud.api.group.ServiceGroup;
import dev.cloud.api.node.CloudNode;

import java.util.UUID;

/**
 * An immutable snapshot of a service's state at a specific point in time.
 * Used for network transport between master and nodes/plugins — does not
 * hold live references to {@link ServiceGroup} or {@link CloudNode}.
 */
public record ServiceSnapshot(
        UUID uniqueId,
        String name,
        String groupName,
        String nodeName,
        ServiceState state,
        ServiceLifecycle lifecycle,
        int port,
        int onlineCount,
        int maxPlayers,
        String containerId
) implements CloudService {

    @Override public UUID getUniqueId()      { return uniqueId; }
    @Override public String getName()        { return name; }
    @Override public int getPort()           { return port; }
    @Override public int getOnlineCount()    { return onlineCount; }
    @Override public int getMaxPlayers()     { return maxPlayers; }
    @Override public String getContainerId() { return containerId; }
    @Override public ServiceState getState() { return state; }
    @Override public ServiceLifecycle getLifecycle() { return lifecycle; }

    /**
     * @throws UnsupportedOperationException always — use {@link #groupName()} on snapshots
     */
    @Override
    public ServiceGroup getGroup() {
        throw new UnsupportedOperationException("Snapshots do not hold live group references. Use groupName() instead.");
    }

    /**
     * @throws UnsupportedOperationException always — use {@link #nodeName()} on snapshots
     */
    @Override
    public CloudNode getNode() {
        throw new UnsupportedOperationException("Snapshots do not hold live node references. Use nodeName() instead.");
    }
}