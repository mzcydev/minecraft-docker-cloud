package dev.cloud.plugin.common;

import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Wraps the gRPC {@link ManagedChannel} connecting the plugin to the master.
 * Provides a clean close method and exposes the channel for stub creation.
 */
public class CloudConnection {

    private static final Logger log = LoggerFactory.getLogger(CloudConnection.class);

    private final ManagedChannel channel;
    private final String serviceName;

    public CloudConnection(ManagedChannel channel, String serviceName) {
        this.channel = channel;
        this.serviceName = serviceName;
    }

    /**
     * Returns the underlying gRPC channel.
     * Used by service/player clients to create stubs.
     */
    public ManagedChannel getChannel() {
        return channel;
    }

    /**
     * Returns the name of this service as registered with the master.
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Returns {@code true} if the channel is still open.
     */
    public boolean isConnected() {
        return !channel.isShutdown() && !channel.isTerminated();
    }

    /**
     * Gracefully closes the gRPC channel.
     */
    public void close() {
        try {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            log.info("gRPC channel closed.");
        } catch (InterruptedException e) {
            channel.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}