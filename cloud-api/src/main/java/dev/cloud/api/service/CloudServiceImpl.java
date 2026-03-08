package dev.cloud.api.service;

import java.util.UUID;

/**
 * Default mutable implementation of {@link CloudService}.
 */
public class CloudServiceImpl implements CloudService {

    private final UUID id;
    private final String name;
    private final String groupName;
    private final String nodeName;
    private final String host;
    private final int maxPlayers;
    private int port;
    private volatile int onlinePlayers;
    private volatile ServiceState state;
    private volatile String containerId;

    public CloudServiceImpl(UUID id, String name, String groupName, String nodeName,
                            String host, int port, int maxPlayers,
                            ServiceState state, String containerId) {
        this.id          = id;
        this.name        = name;
        this.groupName   = groupName;
        this.nodeName    = nodeName;
        this.host        = host;
        this.port        = port;
        this.maxPlayers  = maxPlayers;
        this.state       = state;
        this.containerId = containerId;
    }

    @Override public UUID getId()            { return id; }
    @Override public String getName()        { return name; }
    @Override public String getGroupName()   { return groupName; }
    @Override public String getNodeName()    { return nodeName; }
    @Override public String getHost()        { return host; }
    @Override public int getPort()           { return port; }
    @Override public int getOnlinePlayers()  { return onlinePlayers; }
    @Override public int getMaxPlayers()     { return maxPlayers; }
    @Override public ServiceState getState() { return state; }
    @Override public String getContainerId() { return containerId; }

    public void setState(ServiceState state)        { this.state = state; }
    public void setOnlinePlayers(int count)         { this.onlinePlayers = count; }
    public void setContainerId(String containerId)  { this.containerId = containerId; }
    public void setPort(int port)                   { this.port = port; }
}
