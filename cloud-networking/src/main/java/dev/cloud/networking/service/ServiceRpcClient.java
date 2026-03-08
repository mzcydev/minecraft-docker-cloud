package dev.cloud.networking.service;

import dev.cloud.api.group.ServiceGroup;
import dev.cloud.proto.common.Lifecycle;
import dev.cloud.proto.common.Response;
import dev.cloud.proto.service.*;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class ServiceRpcClient {

    private static final Logger log = LoggerFactory.getLogger(ServiceRpcClient.class);

    private final ServiceControlGrpc.ServiceControlBlockingStub stub;

    public ServiceRpcClient(ManagedChannel channel) {
        this.stub = ServiceControlGrpc.newBlockingStub(channel);
    }

    public StartServiceResponse startService(String serviceName, ServiceGroup group) {
        StartServiceRequest request = StartServiceRequest.newBuilder()
                .setServiceName(serviceName)
                .setGroupName(group.getName())
                .setTemplateName(group.getTemplateName())
                .setMemory(group.getMemory())
                .setJvmFlags(group.getJvmFlags())
                .setLifecycle(group.isStatic() ? Lifecycle.LIFECYCLE_STATIC : Lifecycle.LIFECYCLE_DYNAMIC)
                .build();
        log.info("Sending StartService for '{}'", serviceName);
        return stub.startService(request);
    }

    public Response stopService(String serviceName) {
        StopServiceRequest request = StopServiceRequest.newBuilder()
                .setServiceName(serviceName)
                .setForce(false)
                .build();
        log.info("Sending StopService for '{}'", serviceName);
        return stub.stopService(request);
    }

    public Response copyService(String serviceName, String templateName) {
        return stub.copyService(CopyServiceRequest.newBuilder()
                .setServiceName(serviceName)
                .setTemplateName(templateName)
                .build());
    }
}
