package dev.cloud.networking.group;

import dev.cloud.api.group.*;
import dev.cloud.api.event.EventBus;
import dev.cloud.api.event.events.group.*;
import dev.cloud.api.service.ServiceLifecycle;
import dev.cloud.proto.common.Empty;
import dev.cloud.proto.common.Response;
import dev.cloud.proto.group.*;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

/**
 * Master-side gRPC implementation of {@link GroupServiceGrpc.GroupServiceImplBase}.
 * Exposes group CRUD operations to nodes and plugins over gRPC.
 */
public class GroupRpcService extends GroupServiceGrpc.GroupServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(GroupRpcService.class);

    private final GroupManager groupManager;
    private final EventBus eventBus;

    public GroupRpcService(GroupManager groupManager, EventBus eventBus) {
        this.groupManager = groupManager;
        this.eventBus = eventBus;
    }

    @Override
    public void createGroup(CreateGroupRequest request, StreamObserver<Response> observer) {
        try {
            ServiceGroup group = fromProto(request.getGroup());
            groupManager.createGroup(group);
            eventBus.publish(new GroupCreateEvent(group));
            log.info("Group '{}' created via gRPC.", group.getName());
            observer.onNext(Response.newBuilder().setSuccess(true).build());
        } catch (Exception e) {
            observer.onNext(Response.newBuilder().setSuccess(false).setMessage(e.getMessage()).build());
        }
        observer.onCompleted();
    }

    @Override
    public void updateGroup(UpdateGroupRequest request, StreamObserver<Response> observer) {
        try {
            ServiceGroup group = fromProto(request.getGroup());
            groupManager.updateGroup(group);
            eventBus.publish(new GroupUpdateEvent(group));
            observer.onNext(Response.newBuilder().setSuccess(true).build());
        } catch (Exception e) {
            observer.onNext(Response.newBuilder().setSuccess(false).setMessage(e.getMessage()).build());
        }
        observer.onCompleted();
    }

    @Override
    public void deleteGroup(DeleteGroupRequest request, StreamObserver<Response> observer) {
        try {
            groupManager.getGroup(request.getName()).ifPresent(g ->
                    eventBus.publish(new GroupDeleteEvent(g)));
            groupManager.deleteGroup(request.getName());
            observer.onNext(Response.newBuilder().setSuccess(true).build());
        } catch (Exception e) {
            observer.onNext(Response.newBuilder().setSuccess(false).setMessage(e.getMessage()).build());
        }
        observer.onCompleted();
    }

    @Override
    public void getGroup(GetGroupRequest request, StreamObserver<GetGroupResponse> observer) {
        groupManager.getGroup(request.getName()).ifPresentOrElse(
                group -> observer.onNext(GetGroupResponse.newBuilder().setGroup(toProto(group)).build()),
                ()    -> observer.onNext(GetGroupResponse.getDefaultInstance())
        );
        observer.onCompleted();
    }

    @Override
    public void listGroups(Empty request, StreamObserver<ListGroupsResponse> observer) {
        ListGroupsResponse response = ListGroupsResponse.newBuilder()
                .addAllGroups(groupManager.getAllGroups().stream()
                        .map(this::toProto)
                        .collect(Collectors.toList()))
                .build();
        observer.onNext(response);
        observer.onCompleted();
    }

    // ── Mapping helpers ───────────────────────────────────────────────────────

    private ProtoGroup toProto(ServiceGroup g) {
        return ProtoGroup.newBuilder()
                .setName(g.getName())
                .setServiceType(toProtoType(g.getServiceType()))
                .setTemplateName(g.getTemplateName())
                .setMinOnlineCount(g.getMinOnlineCount())
                .setMaxOnlineCount(g.getMaxOnlineCount())
                .setMaxPlayers(g.getMaxPlayers())
                .setMemory(g.getMemory())
                .setLifecycle(toProtoLifecycle(g.getLifecycle()))
                .setMaintenance(g.isMaintenance())
                .setJvmFlags(g.getJvmFlags())
                .setStartPort(g.getStartPort())
                .build();
    }

    private ServiceGroup fromProto(ProtoGroup p) {
        return new ServiceGroupImpl(
                p.getName(),
                fromProtoType(p.getServiceType()),
                p.getTemplateName(),
                p.getMinOnlineCount(),
                p.getMaxOnlineCount(),
                p.getMaxPlayers(),
                p.getMemory(),
                fromProtoLifecycle(p.getLifecycle()),
                p.getMaintenance(),
                p.getJvmFlags(),
                p.getStartPort()
        );
    }

    private dev.cloud.proto.common.ServiceType toProtoType(ServiceType t) {
        return switch (t) {
            case PAPER      -> dev.cloud.proto.common.ServiceType.SERVICE_TYPE_PAPER;
            case VELOCITY   -> dev.cloud.proto.common.ServiceType.SERVICE_TYPE_VELOCITY;
            case BUNGEECORD -> dev.cloud.proto.common.ServiceType.SERVICE_TYPE_BUNGEECORD;
            case FABRIC     -> dev.cloud.proto.common.ServiceType.SERVICE_TYPE_FABRIC;
        };
    }

    private ServiceType fromProtoType(dev.cloud.proto.common.ServiceType t) {
        return switch (t) {
            case SERVICE_TYPE_VELOCITY   -> ServiceType.VELOCITY;
            case SERVICE_TYPE_BUNGEECORD -> ServiceType.BUNGEECORD;
            case SERVICE_TYPE_FABRIC     -> ServiceType.FABRIC;
            default                      -> ServiceType.PAPER;
        };
    }

    private dev.cloud.proto.common.Lifecycle toProtoLifecycle(ServiceLifecycle l) {
        return switch (l) {
            case STATIC -> dev.cloud.proto.common.Lifecycle.LIFECYCLE_STATIC;
            case MANUAL -> dev.cloud.proto.common.Lifecycle.LIFECYCLE_MANUAL;
            default     -> dev.cloud.proto.common.Lifecycle.LIFECYCLE_DYNAMIC;
        };
    }

    private ServiceLifecycle fromProtoLifecycle(dev.cloud.proto.common.Lifecycle l) {
        return switch (l) {
            case LIFECYCLE_STATIC -> ServiceLifecycle.STATIC;
            case LIFECYCLE_MANUAL -> ServiceLifecycle.MANUAL;
            default               -> ServiceLifecycle.DYNAMIC;
        };
    }
}