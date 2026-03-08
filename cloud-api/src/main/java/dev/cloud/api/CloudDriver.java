package dev.cloud.api;

/**
 * Abstract base for master- and node-side implementations.
 * Each component initializes the {@link CloudAPI} and registers its managers.
 */
public abstract class CloudDriver {

    /**
     * Starts the component, establishes connections and initializes all managers.
     *
     * @throws Exception if startup fails for any reason
     */
    public abstract void start() throws Exception;

    /**
     * Gracefully shuts down the component, stops all services and closes connections.
     *
     * @throws Exception if shutdown fails for any reason
     */
    public abstract void shutdown() throws Exception;

    /**
     * Returns the human-readable name of this component (e.g. "Master", "Node-1").
     */
    public abstract String getComponentName();
}