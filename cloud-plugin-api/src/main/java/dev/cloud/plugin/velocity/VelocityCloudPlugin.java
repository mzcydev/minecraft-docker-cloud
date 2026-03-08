package dev.cloud.plugin.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.cloud.plugin.CloudPlugin;
import dev.cloud.plugin.CloudPluginBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Velocity proxy plugin entry point for the cloud plugin API.
 * Registers player listeners and the server registrar on proxy start.
 */
@Plugin(id = "cloud-plugin", name = "CloudPlugin", version = "1.0.0")
public class VelocityCloudPlugin implements CloudPlugin {

    private static final Logger log = LoggerFactory.getLogger(VelocityCloudPlugin.class);

    private final ProxyServer proxy;
    private CloudPluginBootstrap bootstrap;

    @Inject
    public VelocityCloudPlugin(ProxyServer proxy) {
        this.proxy = proxy;
    }

    @Subscribe
    public void onInit(ProxyInitializeEvent event) {
        try {
            bootstrap = new CloudPluginBootstrap(
                    getMasterHost(), getMasterPort(),
                    CloudPluginBootstrap.requireEnv("AUTH_TOKEN"),
                    getServiceName()
            );
            bootstrap.start();

            // Register listeners
            proxy.getEventManager().register(this,
                    new VelocityPlayerListener(proxy, bootstrap.playerManager(),
                            bootstrap.eventBus(), getServiceName()));

            // Register cloud services as Velocity backend servers
            VelocityServerRegistrar registrar = new VelocityServerRegistrar(
                    proxy, bootstrap.serviceManager());
            registrar.registerAll();

            log.info("VelocityCloudPlugin enabled as '{}'.", getServiceName());
        } catch (Exception e) {
            log.error("Failed to start VelocityCloudPlugin: {}", e.getMessage(), e);
        }
    }

    @Subscribe
    public void onShutdown(ProxyShutdownEvent event) {
        if (bootstrap != null) bootstrap.stop();
        log.info("VelocityCloudPlugin disabled.");
    }

    @Override public String getServiceName() { return CloudPluginBootstrap.requireEnv("SERVICE_NAME"); }
    @Override public String getMasterHost()  { return CloudPluginBootstrap.requireEnv("MASTER_HOST"); }
    @Override public int    getMasterPort()  { return CloudPluginBootstrap.envInt("MASTER_PORT", 9090); }
    @Override public void   onEnable()       { /* handled by @Subscribe */ }
    @Override public void   onDisable()      { /* handled by @Subscribe */ }
}