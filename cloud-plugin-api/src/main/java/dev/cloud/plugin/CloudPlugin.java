package dev.cloud.plugin;

/**
 * Contract for all platform-specific cloud plugin implementations.
 * Implemented by {@code PaperCloudPlugin} and {@code VelocityCloudPlugin}.
 */
public interface CloudPlugin {

    /**
     * Called when the plugin is enabled on the platform.
     * Should establish the gRPC connection and register listeners.
     */
    void onEnable();

    /**
     * Called when the plugin is disabled or the server shuts down.
     * Should cleanly close the gRPC channel and unregister listeners.
     */
    void onDisable();

    /**
     * Returns the name of this service as registered with the master.
     * Read from the {@code SERVICE_NAME} environment variable.
     */
    String getServiceName();

    /**
     * Returns the master host this plugin connects to.
     * Read from the {@code MASTER_HOST} environment variable.
     */
    String getMasterHost();

    /**
     * Returns the master gRPC port.
     * Read from the {@code MASTER_PORT} environment variable.
     */
    int getMasterPort();
}