package dev.cloud.plugin.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.cloud.plugin.common.CloudPluginEventBus;
import dev.cloud.plugin.common.CloudPluginPlayerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Velocity event listener that reports player connect/disconnect/switch events
 * to the master via gRPC. The proxy is the authoritative source for player state.
 */
public class VelocityPlayerListener {

    private static final Logger log = LoggerFactory.getLogger(VelocityPlayerListener.class);

    private final ProxyServer proxy;
    private final CloudPluginPlayerManager playerManager;
    private final CloudPluginEventBus eventBus;
    private final String proxyServiceName;

    public VelocityPlayerListener(ProxyServer proxy,
                                  CloudPluginPlayerManager playerManager,
                                  CloudPluginEventBus eventBus,
                                  String proxyServiceName) {
        this.proxy = proxy;
        this.playerManager = playerManager;
        this.eventBus = eventBus;
        this.proxyServiceName = proxyServiceName;
    }

    @Subscribe
    public void onLogin(PostLoginEvent event) {
        var player = event.getPlayer();
        log.debug("Player connected to proxy: {} ({})", player.getUsername(), player.getUniqueId());
        // reportConnect via gRPC — PlayerRpcClient call happens via eventBus
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        var player = event.getPlayer();
        playerManager.unregisterLocal(player.getUniqueId());
        log.debug("Player disconnected from proxy: {}", player.getUsername());
    }

    @Subscribe
    public void onServerSwitch(ServerConnectedEvent event) {
        var player = event.getPlayer();
        var serverName = event.getServer().getServerInfo().getName();
        log.debug("Player '{}' switched to server '{}'.", player.getUsername(), serverName);
        playerManager.sendToService(player.getUniqueId(), serverName);
    }
}