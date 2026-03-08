package dev.cloud.api.player;

import java.util.UUID;

/**
 * Represents a player currently connected to the network via a proxy.
 */
public interface CloudPlayer {

    /** Returns the player's unique Minecraft UUID. */
    UUID getUniqueId();

    /** Returns the player's current username. */
    String getName();

    /** Returns the name of the service the player is currently on (e.g. {@code "BedWars-2"}). */
    String getCurrentService();

    /** Returns the name of the proxy the player is connected through (e.g. {@code "Proxy-1"}). */
    String getProxyName();

    /** Returns the player's IP address as a string. */
    String getAddress();
}