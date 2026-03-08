package dev.cloud.node.health;

import dev.cloud.networking.node.NodeRpcClient;
import dev.cloud.node.service.NodeServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Sends periodic heartbeat messages to the master via gRPC.
 * Reports current service count and memory usage every 5 seconds.
 */
public class HeartbeatSender {

    private static final Logger log = LoggerFactory.getLogger(HeartbeatSender.class);
    private static final int INTERVAL_SECONDS = 5;

    private final NodeRpcClient rpcClient;
    private final String nodeName;
    private final NodeServiceTracker tracker;
    private final HealthMonitor healthMonitor;

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "heartbeat-sender");
                t.setDaemon(true);
                return t;
            });

    public HeartbeatSender(NodeRpcClient rpcClient, String nodeName,
                           NodeServiceTracker tracker, HealthMonitor healthMonitor) {
        this.rpcClient     = rpcClient;
        this.nodeName      = nodeName;
        this.tracker       = tracker;
        this.healthMonitor = healthMonitor;
    }

    /**
     * Starts sending heartbeats at a fixed interval.
     */
    public void start() {
        scheduler.scheduleAtFixedRate(this::sendHeartbeat,
                INTERVAL_SECONDS, INTERVAL_SECONDS, TimeUnit.SECONDS);
        log.info("Heartbeat sender started (every {}s).", INTERVAL_SECONDS);
    }

    /**
     * Stops the heartbeat scheduler.
     */
    public void stop() {
        scheduler.shutdownNow();
        log.info("Heartbeat sender stopped.");
    }

    private void sendHeartbeat() {
        try {
            int serviceCount = tracker.runningCount();
            long usedMemoryMb = healthMonitor.usedMemoryMb();
            int maxMemoryMb   = healthMonitor.maxMemoryMb();

            rpcClient.sendHeartbeat(nodeName, serviceCount, usedMemoryMb, maxMemoryMb);
            log.debug("Heartbeat sent: {} services, {}MB / {}MB", serviceCount, usedMemoryMb, maxMemoryMb);
        } catch (Exception e) {
            log.warn("Failed to send heartbeat: {}", e.getMessage());
        }
    }
}