package dev.cloud.plugin.paper;

import dev.cloud.plugin.CloudPlugin;
import dev.cloud.plugin.CloudPluginBootstrap;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Paper/Bukkit plugin entry point for the cloud plugin API.
 * Registers event listeners and connects to the master on enable.
 */
public class PaperCloudPlugin extends JavaPlugin implements CloudPlugin {

    private static final Logger log = LoggerFactory.getLogger(PaperCloudPlugin.class);

    private CloudPluginBootstrap bootstrap;

    @Override
    public void onEnable() {
        try {
            bootstrap = new CloudPluginBootstrap(
                    getMasterHost(), getMasterPort(),
                    CloudPluginBootstrap.requireEnv("AUTH_TOKEN"),
                    getServiceName()
            );
            bootstrap.start();

            // Register listeners
            getServer().getPluginManager().registerEvents(
                    new PaperPlayerListener(bootstrap.playerManager(),
                            bootstrap.eventBus(), getServiceName()), this);

            log.info("PaperCloudPlugin enabled as '{}'.", getServiceName());
        } catch (Exception e) {
            log.error("Failed to start PaperCloudPlugin: {}", e.getMessage(), e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (bootstrap != null) bootstrap.stop();
        log.info("PaperCloudPlugin disabled.");
    }

    @Override public String getServiceName() { return CloudPluginBootstrap.requireEnv("SERVICE_NAME"); }
    @Override public String getMasterHost()  { return CloudPluginBootstrap.requireEnv("MASTER_HOST"); }
    @Override public int    getMasterPort()  { return CloudPluginBootstrap.envInt("MASTER_PORT", 9090); }
}