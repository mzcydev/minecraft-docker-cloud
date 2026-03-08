package dev.cloud.api.node;

/**
 * Represents a physical or virtual host that can start and manage Docker containers.
 */
public interface CloudNode {

    /** Returns the unique name of this node (e.g. {@code "Node-1"}). */
    String getName();

    /** Returns the hostname or IP address of this node. */
    String getHost();

    /** Returns the gRPC port this node listens on. */
    int getPort();

    /** Returns the current operational state of this node. */
    NodeState getState();

    /** Returns the total available RAM of this node in megabytes. */
    int getTotalMemory();

    /** Returns the amount of RAM currently allocated to running services in megabytes. */
    int getUsedMemory();

    /** Returns the remaining allocatable RAM in megabytes. */
    default int getAvailableMemory() {
        return getTotalMemory() - getUsedMemory();
    }

    /** Returns the current CPU usage of this node as a percentage (0–100). */
    double getCpuUsage();

    /** Returns the number of services currently running on this node. */
    int getServiceCount();

    /**
     * Returns {@code true} if this node is connected and not draining,
     * and therefore able to accept new services.
     */
    default boolean isAvailable() {
        return getState() == NodeState.CONNECTED;
    }

    /**
     * Returns {@code true} if this node has enough free RAM to host a service
     * requiring the given amount of memory.
     *
     * @param requiredMemoryMb the memory required in megabytes
     */
    default boolean canHost(int requiredMemoryMb) {
        return isAvailable() && getAvailableMemory() >= requiredMemoryMb;
    }
}