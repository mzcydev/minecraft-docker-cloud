package dev.cloud.node.health;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Info;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

/**
 * Reads live resource usage of the node (JVM + Docker daemon info).
 * Used by the heartbeat sender to report node capacity to the master.
 */
public class HealthMonitor {

    private static final Logger log = LoggerFactory.getLogger(HealthMonitor.class);

    private final DockerClient docker;
    private final int maxMemoryMb;
    private final MemoryMXBean memoryBean;

    public HealthMonitor(DockerClient docker, int maxMemoryMb) {
        this.docker     = docker;
        this.maxMemoryMb = maxMemoryMb;
        this.memoryBean = ManagementFactory.getMemoryMXBean();
    }

    /**
     * Returns the current JVM heap usage in megabytes.
     */
    public long usedMemoryMb() {
        return memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
    }

    /**
     * Returns the configured maximum memory for this node in megabytes.
     */
    public int maxMemoryMb() {
        return maxMemoryMb;
    }

    /**
     * Returns the number of available CPU cores on the node.
     */
    public int cpuCores() {
        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * Returns the percentage of heap memory currently in use (0–100).
     */
    public double memoryUsagePercent() {
        long max = memoryBean.getHeapMemoryUsage().getMax();
        long used = memoryBean.getHeapMemoryUsage().getUsed();
        if (max <= 0) return 0;
        return ((double) used / max) * 100.0;
    }

    /**
     * Checks whether the Docker daemon is reachable.
     */
    public boolean isDockerReachable() {
        try {
            docker.pingCmd().exec();
            return true;
        } catch (Exception e) {
            log.warn("Docker daemon unreachable: {}", e.getMessage());
            return false;
        }
    }
}