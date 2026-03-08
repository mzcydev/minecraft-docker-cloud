package dev.cloud.api.network;

/**
 * Abstracts an open gRPC channel to a remote component.
 * Used by clients to send messages without depending on gRPC stubs directly.
 */
public interface NetworkChannel {

    /** Returns the name of the remote component (e.g. node name). */
    String getRemoteName();

    /** Returns the type of the remote component. */
    NetworkComponent getRemoteType();

    /** Returns {@code true} if the channel is currently open and usable. */
    boolean isOpen();

    /** Closes this channel and releases all associated resources. */
    void close();
}