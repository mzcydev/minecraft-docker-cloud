package dev.cloud.api.node;

/**
 * Represents a physical or virtual host that runs Docker containers.
 */
public interface CloudNode {

    String getName();
    String getHost();
    int getPort();
    NodeState getState();
    int getMaxMemoryMb();
    long getUsedMemoryMb();
    int getMaxServices();
    int getRunningServices();
    double getCpuUsage();

    default int getAvailableMemoryMb()              { return (int)(getMaxMemoryMb() - getUsedMemoryMb()); }
    default boolean isAvailable()                   { return getState() == NodeState.CONNECTED; }
    default boolean canHost(int requiredMemoryMb)   { return isAvailable() && getAvailableMemoryMb() >= requiredMemoryMb; }
}
