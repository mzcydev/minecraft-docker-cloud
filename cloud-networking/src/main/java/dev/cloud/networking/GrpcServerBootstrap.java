package dev.cloud.networking;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import io.grpc.BindableService;
import dev.cloud.networking.interceptor.AuthInterceptor;
import dev.cloud.networking.interceptor.LoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Bootstraps and manages the gRPC server on the master side.
 * All RPC service implementations are registered here before the server starts.
 */
public class GrpcServerBootstrap {

    private static final Logger log = LoggerFactory.getLogger(GrpcServerBootstrap.class);

    private final int port;
    private final String authToken;
    private final List<BindableService> services = new ArrayList<>();
    private Server server;

    /**
     * @param port      the port the gRPC server will listen on
     * @param authToken the shared secret used by {@link AuthInterceptor} to validate incoming calls
     */
    public GrpcServerBootstrap(int port, String authToken) {
        this.port = port;
        this.authToken = authToken;
    }

    /**
     * Registers a gRPC service implementation to be served.
     *
     * @param service the service to register
     * @return this instance for chaining
     */
    public GrpcServerBootstrap addService(BindableService service) {
        services.add(service);
        return this;
    }

    /**
     * Builds and starts the gRPC server with all registered services and interceptors.
     *
     * @throws IOException if the server fails to bind to the port
     */
    public void start() throws IOException {
        AuthInterceptor authInterceptor = new AuthInterceptor(authToken);
        LoggingInterceptor loggingInterceptor = new LoggingInterceptor();

        ServerBuilder<?> builder = ServerBuilder.forPort(port);

        for (BindableService service : services) {
            builder.addService(
                    ServerInterceptors.intercept(service, authInterceptor, loggingInterceptor)
            );
        }

        server = builder.build().start();
        log.info("gRPC server started on port {}", port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down gRPC server...");
            stop();
        }));
    }

    /**
     * Blocks the calling thread until the server shuts down.
     *
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public void awaitTermination() throws InterruptedException {
        if (server != null) server.awaitTermination();
    }

    /**
     * Initiates a graceful shutdown, waiting up to 30 seconds for ongoing calls to complete.
     */
    public void stop() {
        if (server == null) return;
        try {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
            log.info("gRPC server shut down.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            server.shutdownNow();
        }
    }

    /** Returns {@code true} if the server is running and has not been shut down. */
    public boolean isRunning() {
        return server != null && !server.isShutdown();
    }
}