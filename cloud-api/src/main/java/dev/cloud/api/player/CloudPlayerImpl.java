package dev.cloud.api.player;

import java.util.UUID;

/**
 * Default mutable implementation of {@link CloudPlayer}.
 */
public class CloudPlayerImpl implements CloudPlayer {

    private final UUID uuid;
    private final String name;
    private final String proxyName;
    private final String address;
    private volatile String currentService;

    public CloudPlayerImpl(UUID uuid, String name, String currentService,
                           String proxyName, String address) {
        this.uuid           = uuid;
        this.name           = name;
        this.currentService = currentService;
        this.proxyName      = proxyName;
        this.address        = address;
    }

    @Override public UUID getUuid()              { return uuid; }
    @Override public String getName()            { return name; }
    @Override public String getCurrentService()  { return currentService; }
    @Override public String getProxyName()       { return proxyName; }
    @Override public String getAddress()         { return address; }

    public void setCurrentService(String service) { this.currentService = service; }
}
