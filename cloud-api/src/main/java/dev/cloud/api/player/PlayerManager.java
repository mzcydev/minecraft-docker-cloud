package dev.cloud.api.player;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages all players currently connected to the network.
 */
public interface PlayerManager {

    /**
     * Registers a player as connected, typically called on proxy login.
     *
     * @param player the player to register
     */
    void registerPlayer(CloudPlayer player);

    /**
     * Unregisters a player, typically called on proxy disconnect.
     *
     * @param uniqueId the UUID of the player to unregister
     */
    void unregisterPlayer(UUID uniqueId);

    /**
     * Looks up a player by UUID.
     *
     * @param uniqueId the player's UUID
     * @return an {@link Optional} containing the player, or empty if not online
     */
    Optional<CloudPlayer> getPlayer(UUID uniqueId);

    /**
     * Looks up a player by username (case-insensitive).
     *
     * @param name the player's username
     * @return an {@link Optional} containing the player, or empty if not online
     */
    Optional<CloudPlayer> getPlayer(String name);

    /**
     * Returns all currently connected players across all proxies.
     */
    Collection<CloudPlayer> getAllPlayers();

    /**
     * Returns the total number of players currently online on the network.
     */
    int getOnlineCount();
}