package dev.cloud.node.grpc;

import dev.cloud.node.NodeCloudAPI;
import dev.cloud.proto.service.ServiceControlGrpc;
import dev.cloud.proto.service.StartServiceRequest;
import dev.cloud.proto.service.StopServiceRequest;
import dev.cloud.proto.service.CopyServiceRequest;
import dev.cloud.proto.common.Response;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Node-side gRPC implementation of {@code ServiceControl}.
 * Receives start/stop/copy commands from the master and delegates to the service manager.
 */
public class NodeServiceControlImpl extends ServiceControlGrpc.ServiceControlImplBase {

    private static final Logger log = LoggerFactory.getLogger(NodeServiceControlImpl.class);

    private final NodeCloudAPI cloudAPI;

    public NodeServiceControlImpl(NodeCloudAPI cloudAPI) {
        this.cloudAPI = cloudAPI;
    }

    @Override
    public void startService(StartServiceRequest request,
                             StreamObserver<Response> responseObserver) {
        String serviceName = request.getServiceName();
        String groupName   = request.getGroupName();
        log.info("Received startService: name={} group={}", serviceName, groupName);

        try {
            // Look up group from request fields and build a minimal ServiceGroup
            // The master sends all needed fields inline in the request
            var group = ProtoGroupMapper.fromStartRequest(request);
            cloudAPI.serviceManager().start(serviceName, group);

            responseObserver.onNext(Response.newBuilder()
                    .setSuccess(true)
                    .setMessage("Service " + serviceName + " starting.")
                    .build());
        } catch (Exception e) {
            log.error("Failed to start service '{}': {}", serviceName, e.getMessage(), e);
            responseObserver.onNext(Response.newBuilder()
                    .setSuccess(false)
                    .setMessage(e.getMessage())
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void stopService(StopServiceRequest request,
                            StreamObserver<Response> responseObserver) {
        String serviceName = request.getServiceName();
        log.info("Received stopService: {}", serviceName);

        try {
            cloudAPI.serviceManager().stop(serviceName, false);
            responseObserver.onNext(Response.newBuilder()
                    .setSuccess(true)
                    .setMessage("Service " + serviceName + " stopped.")
                    .build());
        } catch (Exception e) {
            responseObserver.onNext(Response.newBuilder()
                    .setSuccess(false)
                    .setMessage(e.getMessage())
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void copyService(CopyServiceRequest request,
                            StreamObserver<Response> responseObserver) {
        // Copy = save current service state back into the template
        String serviceName = request.getServiceName();
        log.info("Received copyService: {}", serviceName);

        // For now: acknowledge — full implementation goes in cloud-master
        responseObserver.onNext(Response.newBuilder()
                .setSuccess(true)
                .setMessage("Copy acknowledged.")
                .build());
        responseObserver.onCompleted();
    }
}