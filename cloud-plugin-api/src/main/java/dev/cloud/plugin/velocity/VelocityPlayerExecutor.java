package dev.cloud.plugin.velocity;

import com.velocitypowered.api.proxy.ProxyServer;
import dev.cloud.plugin.common.CloudPluginPlayerManager;

import java.util.UUID;

/**
 * Executes player actions on the Velocity proxy.
 * Wraps the cloud player manager with Velocity-specific implementations
 * for actions that can be performed directly on the proxy (kick, send).
 */
public class VelocityPlayerExecutor {

    private final ProxyServer proxy;
    private final CloudPluginPlayerManager playerManager;

    public VelocityPlayerExecutor(ProxyServer proxy, CloudPluginPlayerManager playerManager) {
        this.proxy         = proxy;
        this.playerManager = playerManager;
    }

    /**
     * Connects a player to a backend server by name.
     *
     * @param uuid       the player's UUID
     * @param serverName the target server registered with Velocity
     */
    public void connect(UUID uuid, String serverName) {
        proxy.getPlayer(uuid).ifPresent(player ->
                proxy.getServer(serverName).ifPresent(server ->
                        player.createConnectionRequest(server).fireAndForget()));
    }

    /**
     * Kicks a player from the proxy with a plain-text reason.
     *
     * @param uuid   the player's UUID
     * @param reason the kick reason
     */
    public void kick(UUID uuid, String reason) {
        proxy.getPlayer(uuid).ifPresent(player ->
                player.disconnect(net.kyori.adventure.text.Component.text(reason)));
    }

    /**
     * Sends a chat message to a player.
     *
     * @param uuid    the player's UUID
     * @param message the message text
     */
    public void sendMessage(UUID uuid, String message) {
        proxy.getPlayer(uuid).ifPresent(player ->
                player.sendMessage(net.kyori.adventure.text.Component.text(message)));
    }
}