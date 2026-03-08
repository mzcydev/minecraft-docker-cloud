package dev.cloud.networking.node;

import dev.cloud.api.event.EventBus;
import dev.cloud.api.event.events.node.NodeConnectEvent;
import dev.cloud.api.event.events.node.NodeDisconnectEvent;
import dev.cloud.api.event.events.node.NodeInfoUpdateEvent;
import dev.cloud.api.node.CloudNodeImpl;
import dev.cloud.api.node.NodeInfo;
import dev.cloud.api.node.NodeManager;
import dev.cloud.api.node.NodeState;
import dev.cloud.networking.GrpcChannelManager;
import dev.cloud.proto.common.Empty;
import dev.cloud.proto.node.*;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeRpcService extends NodeServiceGrpc.NodeServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(NodeRpcService.class);

    private final NodeManager nodeManager;
    private final GrpcChannelManager channelManager;
    private final EventBus eventBus;

    public NodeRpcService(NodeManager nodeManager, GrpcChannelManager channelManager, EventBus eventBus) {
        this.nodeManager    = nodeManager;
        this.channelManager = channelManager;
        this.eventBus       = eventBus;
    }

    @Override
    public void register(RegisterNodeRequest request, StreamObserver<RegisterNodeResponse> observer) {
        String name = request.getNodeName();
        log.info("Node '{}' registering from {}:{}", name, request.getHost(), request.getPort());
        try {
            CloudNodeImpl node = new CloudNodeImpl(
                    name, request.getHost(), request.getPort(),
                    request.getTotalMemory(), 20 // default maxServices
            );
            node.setState(NodeState.CONNECTED);
            nodeManager.registerNode(node);
            channelManager.openChannel(name, request.getHost(), request.getPort());
            eventBus.publish(new NodeConnectEvent(node));
            observer.onNext(RegisterNodeResponse.newBuilder().setAccepted(true).build());
        } catch (Exception e) {
            log.error("Failed to register node '{}'", name, e);
            observer.onNext(RegisterNodeResponse.newBuilder()
                    .setAccepted(false).setRejectReason(e.getMessage()).build());
        }
        observer.onCompleted();
    }

    @Override
    public void heartbeat(HeartbeatRequest request, StreamObserver<HeartbeatResponse> observer) {
        String name = request.getNodeName();
        NodeInfo info = new NodeInfo(name,
                request.getTotalMemory(), request.getUsedMemory(),
                request.getCpuUsage(), request.getServiceCount());
        nodeManager.updateNodeInfo(info);
        nodeManager.getNode(name).ifPresent(node ->
                eventBus.publish(new NodeInfoUpdateEvent(node, info)));
        observer.onNext(HeartbeatResponse.newBuilder().setAcknowledged(true).setDrain(false).build());
        observer.onCompleted();
    }

    @Override
    public void notifyShutdown(ShutdownNotification request, StreamObserver<Empty> observer) {
        String name = request.getNodeName();
        log.info("Node '{}' shutting down: {}", name, request.getReason());
        nodeManager.getNode(name).ifPresent(node ->
                eventBus.publish(new NodeDisconnectEvent(node, request.getReason())));
        nodeManager.unregisterNode(name);
        observer.onNext(Empty.getDefaultInstance());
        observer.onCompleted();
    }
}
