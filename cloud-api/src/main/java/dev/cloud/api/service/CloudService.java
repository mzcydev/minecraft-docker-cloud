package dev.cloud.api.service;

import java.util.UUID;

/**
 * Represents a single running or stopped service instance (e.g. "Lobby-1", "BedWars-3").
 */
public interface CloudService {

    UUID getId();
    String getName();
    String getGroupName();
    String getNodeName();
    String getHost();
    int getPort();
    int getOnlinePlayers();
    int getMaxPlayers();
    ServiceState getState();
    String getContainerId();

    default boolean isFull()   { return getOnlinePlayers() >= getMaxPlayers(); }
    default boolean isOnline() { return getState() == ServiceState.ONLINE; }
}
