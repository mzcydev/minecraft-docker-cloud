package dev.cloud.api.player;

import java.util.UUID;

/**
 * Performs actions on players from the master side,
 * delegated to the appropriate proxy via gRPC.
 */
public interface PlayerExecutor {

    /**
     * Sends a chat message to the given player.
     *
     * @param uniqueId the target player's UUID
     * @param message  the plain-text or MiniMessage formatted message
     */
    void sendMessage(UUID uniqueId, String message);

    /**
     * Kicks the given player from the network with a reason.
     *
     * @param uniqueId the target player's UUID
     * @param reason   the kick reason displayed to the player
     */
    void kick(UUID uniqueId, String reason);

    /**
     * Connects the given player to a different service.
     *
     * @param uniqueId    the target player's UUID
     * @param serviceName the name of the target service (e.g. {@code "BedWars-1"})
     */
    void connect(UUID uniqueId, String serviceName);

    /**
     * Sends a broadcast message to all players currently on the network.
     *
     * @param message the message to broadcast
     */
    void broadcast(String message);
}