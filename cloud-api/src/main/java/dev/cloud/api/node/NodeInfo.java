package dev.cloud.api.node;

/**
 * Immutable heartbeat payload sent periodically by each node to the master.
 * Contains resource usage statistics used for scheduling decisions.
 */
public record NodeInfo(
        String nodeName,
        int totalMemory,
        int usedMemory,
        double cpuUsage,
        int serviceCount
) {
    /**
     * Returns the percentage of RAM currently in use (0–100).
     */
    public double memoryUsagePercent() {
        if (totalMemory == 0) return 0;
        return (usedMemory / (double) totalMemory) * 100.0;
    }
}