package dev.cloud.networking.service;

import dev.cloud.proto.service.*;
import dev.cloud.proto.common.Response;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Master-side stub wrapper for calling a node's {@link ServiceControlGrpc} RPC service.
 * The master uses this to instruct a specific node to start or stop a service.
 */
public class ServiceRpcClient {

    private static final Logger log = LoggerFactory.getLogger(ServiceRpcClient.class);

    private final ServiceControlGrpc.ServiceControlBlockingStub stub;
    private final String nodeName;

    /**
     * @param channel  the open channel to the target node
     * @param nodeName the name of the node (used for logging)
     */
    public ServiceRpcClient(ManagedChannel channel, String nodeName) {
        this.stub = ServiceControlGrpc.newBlockingStub(channel);
        this.nodeName = nodeName;
    }

    /**
     * Instructs the node to start a new service container.
     *
     * @param request the fully populated start request
     * @return the node's response containing success flag and container ID
     */
    public StartServiceResponse startService(StartServiceRequest request) {
        log.info("Sending StartService request for '{}' to node '{}'",
                request.getServiceName(), nodeName);
        return stub.startService(request);
    }

    /**
     * Instructs the node to stop and remove a running service container.
     *
     * @param serviceName the name of the service to stop
     * @param containerId the Docker container ID of the service
     * @param force       if {@code true}, the container is killed immediately
     * @return the node's response
     */
    public Response stopService(String serviceName, String containerId, boolean force) {
        StopServiceRequest request = StopServiceRequest.newBuilder()
                .setServiceName(serviceName)
                .setContainerId(containerId)
                .setForce(force)
                .build();

        log.info("Sending StopService request for '{}' to node '{}' (force={})",
                serviceName, nodeName, force);
        return stub.stopService(request);
    }

    /**
     * Instructs the node to copy a service's files back to its template.
     * Only relevant for {@link dev.cloud.api.service.ServiceLifecycle#STATIC} services.
     *
     * @param serviceName  the service whose files should be saved
     * @param templateName the template to copy files into
     * @return the node's response
     */
    public Response copyService(String serviceName, String templateName) {
        CopyServiceRequest request = CopyServiceRequest.newBuilder()
                .setServiceName(serviceName)
                .setTemplateName(templateName)
                .build();

        log.info("Sending CopyService request for '{}' to node '{}'", serviceName, nodeName);
        return stub.copyService(request);
    }
}