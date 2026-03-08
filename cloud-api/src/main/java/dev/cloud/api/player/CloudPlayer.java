package dev.cloud.api.player;

import java.util.UUID;

/**
 * Represents a player currently connected to the network.
 */
public interface CloudPlayer {

    UUID getUuid();
    String getName();
    String getCurrentService();
    String getProxyName();
    String getAddress();
}
