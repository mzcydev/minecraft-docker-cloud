package dev.cloud.master.node;

import dev.cloud.api.node.CloudNodeImpl;
import dev.cloud.api.node.NodeState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Periodically checks whether connected nodes are still sending heartbeats.
 * If a node's last heartbeat is older than the timeout threshold, it is marked as disconnected.
 */
public class NodeHealthChecker {

    private static final Logger log = LoggerFactory.getLogger(NodeHealthChecker.class);
    private static final int TIMEOUT_SECONDS = 15;
    private static final int CHECK_INTERVAL = 10;

    private final NodeRegistry registry;
    private final MasterNodeManager nodeManager;

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "node-health-checker");
                t.setDaemon(true);
                return t;
            });

    public NodeHealthChecker(NodeRegistry registry, MasterNodeManager nodeManager) {
        this.registry = registry;
        this.nodeManager = nodeManager;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::check,
                CHECK_INTERVAL, CHECK_INTERVAL, TimeUnit.SECONDS);
        log.info("Node health checker started (timeout={}s).", TIMEOUT_SECONDS);
    }

    public void stop() {
        scheduler.shutdownNow();
    }

    private void check() {
        Instant threshold = Instant.now().minusSeconds(TIMEOUT_SECONDS);
        for (CloudNodeImpl node : registry.allNodes()) {
            if (node.getState() != NodeState.CONNECTED) continue;
            if (node.getLastHeartbeat() != null && node.getLastHeartbeat().isBefore(threshold)) {
                log.warn("Node '{}' timed out — no heartbeat since {}.",
                        node.getName(), node.getLastHeartbeat());
                nodeManager.unregister(node.getName());
            }
        }
    }
}