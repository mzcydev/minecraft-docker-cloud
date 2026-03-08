package dev.cloud.networking;

import dev.cloud.networking.interceptor.AuthInterceptor;
import dev.cloud.networking.interceptor.LoggingInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Manages outbound gRPC channels from the master to each connected node.
 * Each node gets its own {@link ManagedChannel} which is kept open for the duration
 * of the node's connection.
 */
public class GrpcChannelManager {

    private static final Logger log = LoggerFactory.getLogger(GrpcChannelManager.class);

    /**
     * Maps node name → open channel.
     */
    private final Map<String, ManagedChannel> channels = new ConcurrentHashMap<>();
    private final String authToken;

    /**
     * @param authToken the shared secret attached to all outgoing calls
     */
    public GrpcChannelManager(String authToken) {
        this.authToken = authToken;
    }

    /**
     * Opens a new channel to the given node and registers it.
     * If a channel for that node already exists it is closed first.
     *
     * @param nodeName the unique node name used as the channel key
     * @param host     the node's hostname or IP
     * @param port     the node's gRPC port
     * @return the newly opened channel
     */
    public ManagedChannel openChannel(String nodeName, String host, int port) {
        closeChannel(nodeName); // clean up any stale channel

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .intercept(new AuthInterceptor(authToken), new LoggingInterceptor())
                .build();

        channels.put(nodeName, channel);
        log.info("Opened gRPC channel to node '{}' at {}:{}", nodeName, host, port);
        return channel;
    }

    /**
     * Returns the open channel for the given node.
     *
     * @param nodeName the node name
     * @return an {@link Optional} with the channel, or empty if the node is not connected
     */
    public Optional<ManagedChannel> getChannel(String nodeName) {
        return Optional.ofNullable(channels.get(nodeName));
    }

    /**
     * Closes and removes the channel for the given node.
     *
     * @param nodeName the node whose channel should be closed
     */
    public void closeChannel(String nodeName) {
        ManagedChannel existing = channels.remove(nodeName);
        if (existing == null) return;
        try {
            existing.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            log.info("Closed gRPC channel to node '{}'", nodeName);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            existing.shutdownNow();
        }
    }

    /**
     * Closes all open channels. Called during master shutdown.
     */
    public void closeAll() {
        channels.keySet().forEach(this::closeChannel);
    }

    /**
     * Returns all currently registered node names with open channels.
     */
    public Collection<String> getConnectedNodes() {
        return Collections.unmodifiableSet(channels.keySet());
    }
}