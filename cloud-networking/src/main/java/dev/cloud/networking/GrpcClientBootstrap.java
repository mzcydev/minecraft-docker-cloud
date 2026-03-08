package dev.cloud.networking;

import dev.cloud.networking.interceptor.AuthInterceptor;
import dev.cloud.networking.interceptor.LoggingInterceptor;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Bootstraps a gRPC client channel on the node (or plugin) side.
 * Creates a single {@link ManagedChannel} to the master and provides it for stub creation.
 */
public class GrpcClientBootstrap {

    private static final Logger log = LoggerFactory.getLogger(GrpcClientBootstrap.class);

    private final String masterHost;
    private final int masterPort;
    private final String authToken;
    private ManagedChannel channel;

    /**
     * @param masterHost the hostname or IP of the master
     * @param masterPort the gRPC port of the master
     * @param authToken  the shared secret sent with every outgoing RPC call
     */
    public GrpcClientBootstrap(String masterHost, int masterPort, String authToken) {
        this.masterHost = masterHost;
        this.masterPort = masterPort;
        this.authToken = authToken;
    }

    /**
     * Opens the channel to the master.
     * Must be called before any stubs are created.
     */
    public void connect() {
        ClientInterceptor auth = new AuthInterceptor(authToken);
        ClientInterceptor logging = new LoggingInterceptor();

        channel = ManagedChannelBuilder
                .forAddress(masterHost, masterPort)
                .usePlaintext()
                .intercept(auth, logging)
                .build();

        log.info("gRPC client channel opened to {}:{}", masterHost, masterPort);
    }

    /**
     * Returns the active {@link ManagedChannel} for stub creation.
     *
     * @throws IllegalStateException if {@link #connect()} has not been called yet
     */
    public ManagedChannel getChannel() {
        if (channel == null) throw new IllegalStateException("Channel not connected yet. Call connect() first.");
        return channel;
    }

    /**
     * Shuts down the channel, waiting up to 10 seconds for pending calls to finish.
     */
    public void shutdown() {
        if (channel == null) return;
        try {
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
            log.info("gRPC client channel closed.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            channel.shutdownNow();
        }
    }

    /**
     * Returns {@code true} if the channel is open and not shut down.
     */
    public boolean isConnected() {
        return channel != null && !channel.isShutdown();
    }
}