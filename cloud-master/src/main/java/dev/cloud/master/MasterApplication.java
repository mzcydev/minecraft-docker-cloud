package dev.cloud.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for the cloud master process.
 */
public class MasterApplication {

    private static final Logger log = LoggerFactory.getLogger(MasterApplication.class);

    public static void main(String[] args) throws Exception {
        log.info("Starting Cloud Master...");

        MasterConfig config = MasterConfig.load();
        MasterBootstrap bootstrap = new MasterBootstrap(config);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown signal received.");
            bootstrap.shutdown();
        }, "shutdown-hook"));

        bootstrap.start();
        Thread.currentThread().join();
    }
}