package dev.cloud.plugin.paper;

import dev.cloud.plugin.common.CloudPluginPlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * Utility class for sending cloud messages to players on Paper servers.
 * Provides convenience wrappers over the gRPC player client.
 */
public class PaperCloudMessenger {

    private final CloudPluginPlayerManager playerManager;
    private final JavaPlugin plugin;

    public PaperCloudMessenger(CloudPluginPlayerManager playerManager, JavaPlugin plugin) {
        this.playerManager = playerManager;
        this.plugin        = plugin;
    }

    /**
     * Sends a player to a different cloud service.
     *
     * @param player      the player to send
     * @param serviceName the target service name
     */
    public void sendToService(Player player, String serviceName) {
        playerManager.sendToService(player.getUniqueId(), serviceName);
    }

    /**
     * Kicks a player from the network.
     *
     * @param player the player to kick
     * @param reason the kick reason
     */
    public void kick(Player player, String reason) {
        playerManager.kick(player.getUniqueId(), reason);
    }

    /**
     * Sends a chat message to a player via the cloud.
     *
     * @param uuid    the target player UUID
     * @param message the message
     */
    public void message(UUID uuid, String message) {
        playerManager.sendMessage(uuid, message);
    }
}