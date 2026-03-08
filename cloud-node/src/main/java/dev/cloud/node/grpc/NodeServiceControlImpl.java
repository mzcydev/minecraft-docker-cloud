package dev.cloud.node.grpc;

import dev.cloud.node.NodeCloudAPI;
import dev.cloud.proto.common.Response;
import dev.cloud.proto.service.*;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeServiceControlImpl extends ServiceControlGrpc.ServiceControlImplBase {

    private static final Logger log = LoggerFactory.getLogger(NodeServiceControlImpl.class);

    private final NodeCloudAPI cloudAPI;

    public NodeServiceControlImpl(NodeCloudAPI cloudAPI) {
        this.cloudAPI = cloudAPI;
    }

    @Override
    public void startService(StartServiceRequest request,
                             StreamObserver<StartServiceResponse> observer) {
        String serviceName = request.getServiceName();
        log.info("startService: name={} group={}", serviceName, request.getGroupName());
        try {
            var group = ProtoGroupMapper.fromStartRequest(request);
            String containerId = cloudAPI.serviceManager().start(serviceName, group);
            observer.onNext(StartServiceResponse.newBuilder()
                    .setSuccess(true)
                    .setContainerId(containerId != null ? containerId : "")
                    .build());
        } catch (Exception e) {
            log.error("Failed to start '{}': {}", serviceName, e.getMessage(), e);
            observer.onNext(StartServiceResponse.newBuilder()
                    .setSuccess(false)
                    .setError(e.getMessage())
                    .build());
        }
        observer.onCompleted();
    }

    @Override
    public void stopService(StopServiceRequest request,
                            StreamObserver<Response> observer) {
        String serviceName = request.getServiceName();
        log.info("stopService: {}", serviceName);
        try {
            cloudAPI.serviceManager().stop(serviceName, request.getForce());
            observer.onNext(Response.newBuilder().setSuccess(true).build());
        } catch (Exception e) {
            observer.onNext(Response.newBuilder()
                    .setSuccess(false).setMessage(e.getMessage()).build());
        }
        observer.onCompleted();
    }

    @Override
    public void copyService(CopyServiceRequest request,
                            StreamObserver<Response> observer) {
        log.info("copyService: {}", request.getServiceName());
        observer.onNext(Response.newBuilder().setSuccess(true)
                .setMessage("Copy acknowledged.").build());
        observer.onCompleted();
    }
}
