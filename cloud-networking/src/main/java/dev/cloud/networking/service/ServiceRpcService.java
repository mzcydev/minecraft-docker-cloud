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

public class ServiceRpcService extends ServiceReporterGrpc.ServiceReporterImplBase {

    private static final Logger log = LoggerFactory.getLogger(ServiceRpcService.class);

    private final ServiceManager serviceManager;
    private final EventBus eventBus;

    public ServiceRpcService(ServiceManager serviceManager, EventBus eventBus) {
        this.serviceManager = serviceManager;
        this.eventBus = eventBus;
    }

    @Override
    public void reportStateChange(ServiceStateUpdate request, StreamObserver<Empty> observer) {
        String serviceName = request.getServiceName();
        ServiceState newState = protoToState(request.getNewState());
        log.debug("State change: '{}' → {}", serviceName, newState);

        serviceManager.getService(serviceName).ifPresent(service -> {
            ServiceState old = service.getState();
            serviceManager.updateState(serviceName, newState);
            eventBus.publish(new ServiceStateChangeEvent(service, old, newState));
        });

        observer.onNext(Empty.getDefaultInstance());
        observer.onCompleted();
    }

    @Override
    public void reportPlayerUpdate(ServicePlayerUpdate request, StreamObserver<Empty> observer) {
        String serviceName = request.getServiceName();
        int count = request.getOnlineCount();
        log.debug("Player update '{}': {} players", serviceName, count);

        serviceManager.getService(serviceName).ifPresent(service -> {
            serviceManager.updatePlayerCount(serviceName, count);
            eventBus.publish(new ServiceUpdateEvent(service));
        });

        observer.onNext(Empty.getDefaultInstance());
        observer.onCompleted();
    }

    private ServiceState protoToState(dev.cloud.proto.common.ServiceState s) {
        return switch (s) {
            case SERVICE_STATE_PREPARED -> ServiceState.PREPARED;
            case SERVICE_STATE_STARTING -> ServiceState.STARTING;
            case SERVICE_STATE_ONLINE   -> ServiceState.ONLINE;
            case SERVICE_STATE_STOPPING -> ServiceState.STOPPING;
            case SERVICE_STATE_STOPPED  -> ServiceState.STOPPED;
            default                     -> ServiceState.UNKNOWN;
        };
    }
}
