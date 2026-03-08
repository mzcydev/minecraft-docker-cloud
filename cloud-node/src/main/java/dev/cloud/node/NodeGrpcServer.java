package dev.cloud.node;

import dev.cloud.networking.interceptor.AuthInterceptor;
import dev.cloud.node.grpc.NodeServiceControlImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Node-side gRPC server that listens for commands from the master
 * (start service, stop service, copy service).
 */
public class NodeGrpcServer {

    private static final Logger log = LoggerFactory.getLogger(NodeGrpcServer.class);

    private final int port;
    private final NodeCloudAPI cloudAPI;
    private final String authToken;
    private Server server;

    public NodeGrpcServer(int port, NodeCloudAPI cloudAPI, String authToken) {
        this.port = port;
        this.cloudAPI = cloudAPI;
        this.authToken = authToken;
    }

    public void start() throws IOException {
        server = ServerBuilder.forPort(port)
                .intercept(new AuthInterceptor(authToken))
                .addService(new NodeServiceControlImpl(cloudAPI))
                .build()
                .start();

        log.info("Node gRPC server listening on port {}", port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (server != null) server.shutdownNow();
        }, "grpc-server-shutdown"));
    }

    public void stop() {
        if (server != null && !server.isShutdown()) {
            server.shutdownNow();
        }
    }
}