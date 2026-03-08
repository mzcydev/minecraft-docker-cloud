package dev.cloud.plugin.paper;

import dev.cloud.api.event.events.PlayerLoginEvent;
import dev.cloud.plugin.common.CloudPluginEventBus;
import dev.cloud.plugin.common.CloudPluginPlayerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bukkit event listener that reports player join/quit events to the master
 * via the cloud plugin's gRPC connection.
 */
public class PaperPlayerListener implements Listener {

    private static final Logger log = LoggerFactory.getLogger(PaperPlayerListener.class);

    private final CloudPluginPlayerManager playerManager;
    private final CloudPluginEventBus eventBus;
    private final String serviceName;

    public PaperPlayerListener(CloudPluginPlayerManager playerManager,
                               CloudPluginEventBus eventBus,
                               String serviceName) {
        this.playerManager = playerManager;
        this.eventBus      = eventBus;
        this.serviceName   = serviceName;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        log.debug("Player joined: {} ({})", player.getName(), player.getUniqueId());
        // Report connect to master via gRPC is handled by proxy (Velocity)
        // Paper only tracks locally
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        var player = event.getPlayer();
        playerManager.unregisterLocal(player.getUniqueId());
        log.debug("Player left: {} ({})", player.getName(), player.getUniqueId());
    }
}