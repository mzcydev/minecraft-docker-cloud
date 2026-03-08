package dev.cloud.networking.service;

import dev.cloud.api.event.EventBus;
import dev.cloud.api.event.events.service.ServiceStateChangeEvent;
import dev.cloud.api.event.events.service.ServiceUpdateEvent;
import dev.cloud.api.service.ServiceManager;
import dev.cloud.api.service.ServiceState;
import dev.cloud.proto.common.Empty;
import dev.cloud.proto.service.ServicePlayerUpdate;
import dev.cloud.proto.service.ServiceReporterGrpc;
import dev.cloud.proto.service.ServiceStateUpdate;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Master-side gRPC implementation of {@link ServiceReporterGrpc.ServiceReporterImplBase}.
 * Receives state and player count updates from nodes and applies them to the service registry.
 */
public class ServiceRpcService extends ServiceReporterGrpc.ServiceReporterImplBase {

    private static final Logger log = LoggerFactory.getLogger(ServiceRpcService.class);

    private final ServiceManager serviceManager;
    private final EventBus eventBus;

    public ServiceRpcService(ServiceManager serviceManager, EventBus eventBus) {
        this.serviceManager = serviceManager;
        this.eventBus = eventBus;
    }

    /**
     * Called by a node when a service transitions to a new {@link ServiceState}.
     * Updates the service registry and fires a {@link ServiceStateChangeEvent}.
     */
    @Override
    public void reportStateChange(ServiceStateUpdate request, StreamObserver<Empty> observer) {
        String serviceName = request.getServiceName();
        ServiceState newState = protoToState(request.getNewState());

        log.debug("State change reported for '{}': {}", serviceName, newState);

        serviceManager.getService(serviceName).ifPresent(service -> {
            ServiceState oldState = service.getState();
            // MasterServiceManager handles the actual state mutation
            eventBus.publish(new ServiceStateChangeEvent(service, oldState, newState));
        });

        observer.onNext(Empty.getDefaultInstance());
        observer.onCompleted();
    }

    /**
     * Called by a node when a service's player count changes.
     * Updates the service registry and fires a {@link ServiceUpdateEvent}.
     */
    @Override
    public void reportPlayerUpdate(ServicePlayerUpdate request, StreamObserver<Empty> observer) {
        String serviceName = request.getServiceName();
        int onlineCount = request.getOnlineCount();

        log.debug("Player update for '{}': {} players", serviceName, onlineCount);

        serviceManager.getService(serviceName).ifPresent(service -> {
            eventBus.publish(new ServiceUpdateEvent(service));
        });

        observer.onNext(Empty.getDefaultInstance());
        observer.onCompleted();
    }

    private ServiceState protoToState(dev.cloud.proto.common.ServiceState protoState) {
        return switch (protoState) {
            case SERVICE_STATE_PREPARED -> ServiceState.PREPARED;
            case SERVICE_STATE_STARTING -> ServiceState.STARTING;
            case SERVICE_STATE_ONLINE -> ServiceState.ONLINE;
            case SERVICE_STATE_STOPPING -> ServiceState.STOPPING;
            case SERVICE_STATE_STOPPED -> ServiceState.STOPPED;
            default -> ServiceState.UNKNOWN;
        };
    }
}