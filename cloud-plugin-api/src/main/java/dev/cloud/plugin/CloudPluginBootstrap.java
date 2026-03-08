package dev.cloud.plugin;

import dev.cloud.networking.GrpcClientBootstrap;
import dev.cloud.plugin.common.CloudConnection;
import dev.cloud.plugin.common.CloudPluginEventBus;
import dev.cloud.plugin.common.CloudPluginPlayerManager;
import dev.cloud.plugin.common.CloudPluginServiceManager;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared bootstrap logic for all platform plugins.
 * Establishes the gRPC connection to the master and initializes
 * all shared cloud subsystems (services, players, events).
 * <p>
 * Usage: instantiate in {@link CloudPlugin#onEnable()}, call {@link #start()}.
 */
public class CloudPluginBootstrap {

    private static final Logger log = LoggerFactory.getLogger(CloudPluginBootstrap.class);

    private final String masterHost;
    private final int masterPort;
    private final String authToken;
    private final String serviceName;

    private CloudConnection connection;
    private CloudPluginServiceManager serviceManager;
    private CloudPluginPlayerManager playerManager;
    private CloudPluginEventBus eventBus;

    public CloudPluginBootstrap(String masterHost, int masterPort,
                                String authToken, String serviceName) {
        this.masterHost = masterHost;
        this.masterPort = masterPort;
        this.authToken = authToken;
        this.serviceName = serviceName;
    }

    /**
     * Reads a required environment variable, throwing if it is missing.
     *
     * @param key the environment variable name
     * @return the value
     * @throws IllegalStateException if the variable is not set
     */
    public static String requireEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required environment variable not set: " + key);
        }
        return value;
    }

    /**
     * Reads an integer environment variable with a fallback default.
     *
     * @param key          the environment variable name
     * @param defaultValue the value to use if the variable is not set
     */
    public static int envInt(String key, int defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // ── accessors ─────────────────────────────────────────────────────────────

    /**
     * Establishes the connection and initializes all subsystems.
     *
     * @throws Exception if the gRPC channel cannot be opened
     */
    public void start() throws Exception {
        GrpcClientBootstrap grpc = new GrpcClientBootstrap(masterHost, masterPort, authToken);
        grpc.connect();
        ManagedChannel channel = grpc.getChannel();

        eventBus = new CloudPluginEventBus();
        connection = new CloudConnection(channel, serviceName);
        serviceManager = new CloudPluginServiceManager(channel, eventBus);
        playerManager = new CloudPluginPlayerManager(channel, eventBus);

        log.info("Cloud plugin connected to master at {}:{} as '{}'",
                masterHost, masterPort, serviceName);
    }

    /**
     * Closes the gRPC channel and shuts down all subsystems.
     */
    public void stop() {
        if (connection != null) connection.close();
        log.info("Cloud plugin disconnected from master.");
    }

    public CloudConnection connection() {
        return connection;
    }

    public CloudPluginServiceManager serviceManager() {
        return serviceManager;
    }

    public CloudPluginPlayerManager playerManager() {
        return playerManager;
    }

    public CloudPluginEventBus eventBus() {
        return eventBus;
    }
}