package dev.cloud.api.node;

/**
 * Mutable default implementation of {@link CloudNode}.
 * Updated by the master whenever a heartbeat is received from the node.
 */
public class CloudNodeImpl implements CloudNode {

    private final String name;
    private final String host;
    private final int port;
    private volatile NodeState state;
    private volatile int totalMemory;
    private volatile int usedMemory;
    private volatile double cpuUsage;
    private volatile int serviceCount;

    public CloudNodeImpl(String name, String host, int port, int totalMemory) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.totalMemory = totalMemory;
        this.state = NodeState.CONNECTING;
    }

    @Override public String getName()      { return name; }
    @Override public String getHost()      { return host; }
    @Override public int getPort()         { return port; }
    @Override public NodeState getState()  { return state; }
    @Override public int getTotalMemory()  { return totalMemory; }
    @Override public int getUsedMemory()   { return usedMemory; }
    @Override public double getCpuUsage()  { return cpuUsage; }
    @Override public int getServiceCount() { return serviceCount; }

    /**
     * Applies an updated {@link NodeInfo} heartbeat payload to this node's state.
     *
     * @param info the latest heartbeat data received from the node
     */
    public void applyInfo(NodeInfo info) {
        this.totalMemory  = info.totalMemory();
        this.usedMemory   = info.usedMemory();
        this.cpuUsage     = info.cpuUsage();
        this.serviceCount = info.serviceCount();
    }

    /**
     * Updates the connection state of this node.
     *
     * @param state the new state
     */
    public void setState(NodeState state) {
        this.state = state;
    }
}