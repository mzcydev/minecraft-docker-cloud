package dev.cloud.api.service;

/**
 * Represents the current lifecycle state of a {@link CloudService}.
 */
public enum ServiceState {

    /**
     * Template has been copied, container has not been started yet.
     */
    PREPARED,

    /**
     * Container is running, Minecraft server is booting.
     */
    STARTING,

    /**
     * Server has reported itself as ready to accept players.
     */
    ONLINE,

    /**
     * Shutdown has been initiated, server is in the process of stopping.
     */
    STOPPING,

    /**
     * Container has been stopped and removed.
     */
    STOPPED,

    /**
     * State cannot be determined, e.g. after a node disconnect.
     */
    UNKNOWN
}