package dev.cloud.networking.service;

import dev.cloud.api.service.ServiceState;
import dev.cloud.proto.service.ServicePlayerUpdate;
import dev.cloud.proto.service.ServiceReporterGrpc;
import dev.cloud.proto.service.ServiceStateUpdate;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Node-side client for reporting service state/player changes to the master.
 */
public class ServiceReporterClient {

    private static final Logger log = LoggerFactory.getLogger(ServiceReporterClient.class);

    private final ServiceReporterGrpc.ServiceReporterBlockingStub stub;

    public ServiceReporterClient(ManagedChannel channel) {
        this.stub = ServiceReporterGrpc.newBlockingStub(channel);
    }

    public void reportStateChange(String serviceName, ServiceState state) {
        try {
            stub.reportStateChange(ServiceStateUpdate.newBuilder()
                    .setServiceName(serviceName)
                    .setNewState(toProto(state))
                    .build());
        } catch (Exception e) {
            log.warn("Failed to report state change for '{}': {}", serviceName, e.getMessage());
        }
    }

    public void reportPlayerUpdate(String serviceName, int onlineCount) {
        try {
            stub.reportPlayerUpdate(ServicePlayerUpdate.newBuilder()
                    .setServiceName(serviceName)
                    .setOnlineCount(onlineCount)
                    .build());
        } catch (Exception e) {
            log.warn("Failed to report player update for '{}': {}", serviceName, e.getMessage());
        }
    }

    private dev.cloud.proto.common.ServiceState toProto(ServiceState s) {
        return switch (s) {
            case PREPARED -> dev.cloud.proto.common.ServiceState.SERVICE_STATE_PREPARED;
            case STARTING -> dev.cloud.proto.common.ServiceState.SERVICE_STATE_STARTING;
            case ONLINE   -> dev.cloud.proto.common.ServiceState.SERVICE_STATE_ONLINE;
            case STOPPING -> dev.cloud.proto.common.ServiceState.SERVICE_STATE_STOPPING;
            case STOPPED  -> dev.cloud.proto.common.ServiceState.SERVICE_STATE_STOPPED;
            default       -> dev.cloud.proto.common.ServiceState.SERVICE_STATE_UNSPECIFIED;
        };
    }
}
