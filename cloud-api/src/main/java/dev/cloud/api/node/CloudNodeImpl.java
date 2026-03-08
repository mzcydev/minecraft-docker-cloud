package dev.cloud.api.node;

import java.time.Instant;

/**
 * Mutable default implementation of {@link CloudNode}.
 * Updated by the master whenever a heartbeat is received.
 */
public class CloudNodeImpl implements CloudNode {

    private final String name;
    private final String host;
    private final int port;
    private final int maxServices;
    private volatile NodeState state;
    private volatile int maxMemoryMb;
    private volatile long usedMemoryMb;
    private volatile double cpuUsage;
    private volatile int runningServices;
    private volatile Instant lastHeartbeat;

    public CloudNodeImpl(String name, String host, int port, int maxMemoryMb, int maxServices) {
        this.name        = name;
        this.host        = host;
        this.port        = port;
        this.maxMemoryMb = maxMemoryMb;
        this.maxServices = maxServices;
        this.state       = NodeState.CONNECTING;
    }

    @Override public String getName()          { return name; }
    @Override public String getHost()          { return host; }
    @Override public int getPort()             { return port; }
    @Override public NodeState getState()      { return state; }
    @Override public int getMaxMemoryMb()      { return maxMemoryMb; }
    @Override public long getUsedMemoryMb()    { return usedMemoryMb; }
    @Override public int getMaxServices()      { return maxServices; }
    @Override public int getRunningServices()  { return runningServices; }
    @Override public double getCpuUsage()      { return cpuUsage; }

    public Instant getLastHeartbeat()          { return lastHeartbeat; }

    public void setState(NodeState state)          { this.state = state; }
    public void setUsedMemoryMb(long mb)           { this.usedMemoryMb = mb; }
    public void setRunningServices(int count)      { this.runningServices = count; }
    public void setCpuUsage(double cpu)            { this.cpuUsage = cpu; }
    public void setLastHeartbeat(Instant instant)  { this.lastHeartbeat = instant; }

    public void applyInfo(NodeInfo info) {
        this.maxMemoryMb     = info.totalMemory();
        this.usedMemoryMb    = info.usedMemory();
        this.cpuUsage        = info.cpuUsage();
        this.runningServices = info.serviceCount();
        this.lastHeartbeat   = Instant.now();
    }
}
