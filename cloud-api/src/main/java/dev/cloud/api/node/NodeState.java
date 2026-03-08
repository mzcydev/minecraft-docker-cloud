package dev.cloud.api.node;

/**
 * Represents the current connection and operational state of a {@link CloudNode}.
 */
public enum NodeState {

    /** Node is in the process of registering with the master. */
    CONNECTING,

    /** Node is fully connected and accepting new services. */
    CONNECTED,

    /** Node is draining: no new services will be started, existing ones run to completion. */
    DRAINING,

    /** Node has disconnected from the master, either gracefully or due to a timeout. */
    DISCONNECTED
}