package dev.cloud.api.player;

import java.util.UUID;

/**
 * Default mutable implementation of {@link CloudPlayer}.
 * The master updates {@link #currentService} when a player switches servers.
 */
public class CloudPlayerImpl implements CloudPlayer {

    private final UUID uniqueId;
    private final String name;
    private final String proxyName;
    private final String address;
    private volatile String currentService;

    public CloudPlayerImpl(UUID uniqueId, String name, String currentService,
                           String proxyName, String address) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.currentService = currentService;
        this.proxyName = proxyName;
        this.address = address;
    }

    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getCurrentService() {
        return currentService;
    }

    /**
     * Updates the service the player is currently on.
     *
     * @param serviceName the name of the new service
     */
    public void setCurrentService(String serviceName) {
        this.currentService = serviceName;
    }

    @Override
    public String getProxyName() {
        return proxyName;
    }

    @Override
    public String getAddress() {
        return address;
    }
}