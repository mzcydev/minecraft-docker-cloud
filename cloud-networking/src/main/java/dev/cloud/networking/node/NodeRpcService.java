package dev.cloud.networking.node;

import dev.cloud.api.node.CloudNodeImpl;
import dev.cloud.api.node.NodeInfo;
import dev.cloud.api.node.NodeManager;
import dev.cloud.api.node.NodeState;
import dev.cloud.api.event.EventBus;
import dev.cloud.api.event.events.node.NodeConnectEvent;
import dev.cloud.api.event.events.node.NodeDisconnectEvent;
import dev.cloud.api.event.events.node.NodeInfoUpdateEvent;
import dev.cloud.networking.GrpcChannelManager;
import dev.cloud.proto.node.*;
import dev.cloud.proto.common.Empty;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Master-side gRPC implementation of {@link NodeServiceGrpc.NodeServiceImplBase}.
 * Handles node registration, heartbeats and shutdown notifications.
 */
public class NodeRpcService extends NodeServiceGrpc.NodeServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(NodeRpcService.class);

    private final NodeManager nodeManager;
    private final GrpcChannelManager channelManager;
    private final EventBus eventBus;

    public NodeRpcService(NodeManager nodeManager, GrpcChannelManager channelManager, EventBus eventBus) {
        this.nodeManager = nodeManager;
        this.channelManager = channelManager;
        this.eventBus = eventBus;
    }

    /**
     * Called when a node connects for the first time.
     * Registers the node, opens a back-channel and fires a {@link NodeConnectEvent}.
     */
    @Override
    public void register(RegisterNodeRequest request, StreamObserver<RegisterNodeResponse> observer) {
        String name = request.getNodeName();
        log.info("Node '{}' attempting to register from {}:{}", name, request.getHost(), request.getPort());

        try {
            CloudNodeImpl node = new CloudNodeImpl(
                    name,
                    request.getHost(),
                    request.getPort(),
                    request.getTotalMemory()
            );
            node.setState(NodeState.CONNECTED);
            nodeManager.registerNode(node);

            // open outbound channel so master can call ServiceControl on this node
            channelManager.openChannel(name, request.getHost(), request.getPort());

            eventBus.publish(new NodeConnectEvent(node));
            log.info("Node '{}' registered successfully.", name);

            observer.onNext(RegisterNodeResponse.newBuilder().setAccepted(true).build());
        } catch (Exception e) {
            log.error("Failed to register node '{}'", name, e);
            observer.onNext(RegisterNodeResponse.newBuilder()
                    .setAccepted(false)
                    .setRejectReason(e.getMessage())
                    .build());
        }

        observer.onCompleted();
    }

    /**
     * Called periodically by nodes to report their resource usage.
     * Updates the node's in-memory state and fires a {@link NodeInfoUpdateEvent}.
     * Returns a drain flag if the node should stop accepting new services.
     */
    @Override
    public void heartbeat(HeartbeatRequest request, StreamObserver<HeartbeatResponse> observer) {
        String name = request.getNodeName();

        NodeInfo info = new NodeInfo(
                name,
                request.getTotalMemory(),
                request.getUsedMemory(),
                request.getCpuUsage(),
                request.getServiceCount()
        );

        nodeManager.updateNodeInfo(info);

        nodeManager.getNode(name).ifPresent(node -> {
            eventBus.publish(new NodeInfoUpdateEvent(node, info));
        });

        observer.onNext(HeartbeatResponse.newBuilder()
                .setAcknowledged(true)
                .setDrain(false) // drain logic lives in MasterNodeManager
                .build());
        observer.onCompleted();
    }

    /**
     * Called by a node just before it shuts down gracefully.
     * Marks the node as disconnected, closes its channel and fires a {@link NodeDisconnectEvent}.
     */
    @Override
    public void notifyShutdown(ShutdownNotification request, StreamObserver<Empty> observer) {
        String name = request.getNodeName();
        log.info("Node '{}' notified shutdown: {}", name, request.getReason());

        nodeManager.getNode(name).ifPresent(node -> {
            if (node instanceof CloudNodeImpl impl) impl.setState(NodeState.DISCONNECTED);
            eventBus.publish(new NodeDisconnectEvent(node, request.getReason()));
        });

        nodeManager.unregisterNode(name);
        channelManager.closeChannel(name);

        observer.onNext(Empty.getDefaultInstance());
        observer.onCompleted();
    }
}