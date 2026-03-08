package dev.cloud.api.network;

/**
 * Identifies the type of component participating in the cloud network.
 */
public enum NetworkComponent {
    /**
     * The central master process.
     */
    MASTER,
    /**
     * A node daemon running on a host machine.
     */
    NODE,
    /**
     * A Velocity or Paper plugin connected to the master.
     */
    PLUGIN
}