package dev.cloud.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for the cloud node daemon.
 */
public class NodeApplication {

    private static final Logger log = LoggerFactory.getLogger(NodeApplication.class);

    public static void main(String[] args) throws Exception {
        log.info("Starting Cloud Node...");

        NodeConfig config = NodeConfig.load();
        NodeBootstrap bootstrap = new NodeBootstrap(config);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown signal received.");
            bootstrap.shutdown();
        }, "shutdown-hook"));

        bootstrap.start();
        Thread.currentThread().join();
    }
}