package dev.cloud.networking.node;

import dev.cloud.proto.node.*;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Node-side stub wrapper for calling the master's {@link NodeServiceGrpc} RPC service.
 * Used by the node daemon to register itself and send periodic heartbeats.
 */
public class NodeRpcClient {

    private static final Logger log = LoggerFactory.getLogger(NodeRpcClient.class);

    private final NodeServiceGrpc.NodeServiceBlockingStub stub;

    /**
     * @param channel the open channel to the master created by {@link dev.cloud.networking.GrpcClientBootstrap}
     */
    public NodeRpcClient(ManagedChannel channel) {
        this.stub = NodeServiceGrpc.newBlockingStub(channel);
    }

    /**
     * Sends a registration request to the master.
     *
     * @param nodeName      this node's unique name
     * @param host          this node's hostname/IP
     * @param port          this node's gRPC port
     * @param totalMemory   total RAM in MB
     * @param cpuCores      number of logical CPU cores
     * @param os            operating system name
     * @param javaVersion   current Java version string
     * @param dockerVersion Docker daemon version string
     * @param authToken     shared secret for authentication
     * @return the master's registration response
     */
    public RegisterNodeResponse register(
            String nodeName, String host, int port,
            int totalMemory, int cpuCores,
            String os, String javaVersion, String dockerVersion,
            String authToken
    ) {
        RegisterNodeRequest request = RegisterNodeRequest.newBuilder()
                .setNodeName(nodeName)
                .setHost(host)
                .setPort(port)
                .setTotalMemory(totalMemory)
                .setCpuCores(cpuCores)
                .setOs(os)
                .setJavaVersion(javaVersion)
                .setDockerVersion(dockerVersion)
                .setAuthToken(authToken)
                .build();

        log.info("Sending registration request to master as '{}'", nodeName);
        return stub.register(request);
    }

    /**
     * Sends a heartbeat with current resource usage to the master.
     *
     * @param nodeName     this node's name
     * @param usedMemory   currently used RAM in MB
     * @param totalMemory  total RAM in MB
     * @param cpuUsage     current CPU usage percentage (0–100)
     * @param serviceCount number of services currently running on this node
     * @return the master's heartbeat acknowledgement
     */
    public HeartbeatResponse sendHeartbeat(
            String nodeName, int usedMemory, int totalMemory,
            double cpuUsage, int serviceCount
    ) {
        HeartbeatRequest request = HeartbeatRequest.newBuilder()
                .setNodeName(nodeName)
                .setUsedMemory(usedMemory)
                .setTotalMemory(totalMemory)
                .setCpuUsage(cpuUsage)
                .setServiceCount(serviceCount)
                .setTimestamp(System.currentTimeMillis())
                .build();

        return stub.heartbeat(request);
    }

    /**
     * Notifies the master that this node is shutting down gracefully.
     *
     * @param nodeName the name of this node
     * @param reason   a human-readable shutdown reason
     */
    public void notifyShutdown(String nodeName, String reason) {
        ShutdownNotification notification = ShutdownNotification.newBuilder()
                .setNodeName(nodeName)
                .setReason(reason)
                .build();

        log.info("Sending shutdown notification to master for node '{}'", nodeName);
        stub.notifyShutdown(notification);
    }
}