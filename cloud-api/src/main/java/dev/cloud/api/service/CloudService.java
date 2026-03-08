package dev.cloud.api.service;

import dev.cloud.api.group.ServiceGroup;
import dev.cloud.api.node.CloudNode;

import java.util.UUID;

/**
 * Represents a single running or stopped service instance (e.g. "Lobby-1", "BedWars-3").
 * A service always belongs to a {@link ServiceGroup} and runs on a {@link CloudNode}.
 */
public interface CloudService {

    /**
     * Returns the unique identifier of this service instance.
     */
    UUID getUniqueId();

    /**
     * Returns the display name, e.g. {@code "Lobby-1"}.
     */
    String getName();

    /**
     * Returns the group this service belongs to.
     */
    ServiceGroup getGroup();

    /**
     * Returns the node this service is running on.
     */
    CloudNode getNode();

    /**
     * Returns the current lifecycle state of this service.
     */
    ServiceState getState();

    /**
     * Returns the lifecycle type that governs how this service is managed.
     */
    ServiceLifecycle getLifecycle();

    /**
     * Returns the port this service is listening on.
     */
    int getPort();

    /**
     * Returns the number of players currently connected to this service.
     */
    int getOnlineCount();

    /**
     * Returns the Docker container ID, or an empty string if not started yet.
     */
    String getContainerId();

    /**
     * Returns the maximum number of players allowed on this service.
     */
    default int getMaxPlayers() {
        return getGroup().getMaxPlayers();
    }

    /**
     * Returns {@code true} if the service has reached its player capacity.
     */
    default boolean isFull() {
        return getOnlineCount() >= getMaxPlayers();
    }

    /**
     * Returns {@code true} if the service is in {@link ServiceState#ONLINE} state.
     */
    default boolean isOnline() {
        return getState() == ServiceState.ONLINE;
    }
}