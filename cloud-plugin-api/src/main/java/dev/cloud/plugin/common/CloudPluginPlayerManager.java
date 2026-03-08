package dev.cloud.plugin.common;

import dev.cloud.api.player.CloudPlayer;
import dev.cloud.networking.player.PlayerRpcClient;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Plugin-side player manager.
 * Tracks players connected to this specific service and provides
 * methods to execute player actions via gRPC.
 */
public class CloudPluginPlayerManager {

    private static final Logger log = LoggerFactory.getLogger(CloudPluginPlayerManager.class);

    private final PlayerRpcClient rpcClient;
    private final CloudPluginEventBus eventBus;

    /** Players currently on this service instance. */
    private final Map<UUID, CloudPlayer> localPlayers = new ConcurrentHashMap<>();

    public CloudPluginPlayerManager(ManagedChannel channel, CloudPluginEventBus eventBus) {
        this.rpcClient = new PlayerRpcClient(channel);
        this.eventBus  = eventBus;
    }

    /**
     * Sends a player to a different service.
     *
     * @param uuid        the player's UUID
     * @param serviceName the target service name
     */
    public void sendToService(UUID uuid, String serviceName) {
        rpcClient.sendPlayer(uuid.toString(), serviceName);
        log.debug("Sending player {} to service '{}'.", uuid, serviceName);
    }

    /**
     * Kicks a player from the network with a reason.
     *
     * @param uuid   the player's UUID
     * @param reason the kick reason shown to the player
     */
    public void kick(UUID uuid, String reason) {
        rpcClient.kickPlayer(uuid.toString(), reason);
        log.debug("Kicking player {}: {}", uuid, reason);
    }

    /**
     * Sends a chat message to a player.
     *
     * @param uuid    the player's UUID
     * @param message the message to send
     */
    public void sendMessage(UUID uuid, String message) {
        rpcClient.messagePlayer(uuid.toString(), message);
    }

    /**
     * Registers a player as connected to this service.
     * Called by the platform listener on join.
     */
    public void registerLocal(CloudPlayer player) {
        localPlayers.put(player.getUuid(), player);
    }

    /**
     * Removes a player from the local tracking map.
     * Called by the platform listener on quit.
     */
    public void unregisterLocal(UUID uuid) {
        localPlayers.remove(uuid);
    }

    /**
     * Returns all players currently on this service.
     */
    public Collection<CloudPlayer> localPlayers() {
        return localPlayers.values();
    }

    /**
     * Finds a locally connected player by UUID.
     */
    public Optional<CloudPlayer> findLocal(UUID uuid) {
        return Optional.ofNullable(localPlayers.get(uuid));
    }
}