package dev.cloud.plugin.velocity;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import dev.cloud.api.service.CloudService;
import dev.cloud.api.service.ServiceState;
import dev.cloud.plugin.common.CloudPluginServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Registers cloud services as backend servers in the Velocity proxy.
 * Called at startup to populate Velocity's server list from the master's service registry.
 * Also provides methods to dynamically add/remove servers as services start and stop.
 */
public class VelocityServerRegistrar {

    private static final Logger log = LoggerFactory.getLogger(VelocityServerRegistrar.class);

    private final ProxyServer proxy;
    private final CloudPluginServiceManager serviceManager;

    public VelocityServerRegistrar(ProxyServer proxy, CloudPluginServiceManager serviceManager) {
        this.proxy = proxy;
        this.serviceManager = serviceManager;
    }

    /**
     * Registers all currently known online services as Velocity backend servers.
     */
    public void registerAll() {
        serviceManager.all().stream()
                .filter(s -> s.getState() == ServiceState.ONLINE)
                .forEach(this::register);
        log.info("Registered {} cloud services with Velocity.", serviceManager.all().size());
    }

    /**
     * Registers a single service as a Velocity backend server.
     *
     * @param service the service to register
     */
    public void register(CloudService service) {
        ServerInfo info = new ServerInfo(
                service.getName(),
                new InetSocketAddress(service.getHost(), service.getPort())
        );
        proxy.registerServer(info);
        log.debug("Registered server '{}' at {}:{}", service.getName(),
                service.getHost(), service.getPort());
    }

    /**
     * Unregisters a service from the Velocity server list.
     *
     * @param serviceName the service name to unregister
     */
    public void unregister(String serviceName) {
        proxy.getServer(serviceName).ifPresent(server -> {
            proxy.unregisterServer(server.getServerInfo());
            log.debug("Unregistered server '{}'.", serviceName);
        });
    }
}