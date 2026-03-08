package dev.cloud.networking.group;

import dev.cloud.api.group.GroupManager;
import dev.cloud.api.group.ServiceGroup;
import dev.cloud.api.group.ServiceGroupImpl;
import dev.cloud.api.group.ServiceType;
import dev.cloud.proto.common.Empty;
import dev.cloud.proto.common.Response;
import dev.cloud.proto.group.*;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

public class GroupRpcService extends GroupServiceGrpc.GroupServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(GroupRpcService.class);

    private final GroupManager groupManager;

    public GroupRpcService(GroupManager groupManager) {
        this.groupManager = groupManager;
    }

    @Override
    public void createGroup(CreateGroupRequest request, StreamObserver<Response> observer) {
        try {
            ServiceGroup group = fromProto(request.getGroup());
            groupManager.createGroup(group);
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
            observer.onNext(Response.newBuilder().setSuccess(true).build());
        } catch (Exception e) {
            observer.onNext(Response.newBuilder().setSuccess(false).setMessage(e.getMessage()).build());
        }
        observer.onCompleted();
    }

    @Override
    public void deleteGroup(DeleteGroupRequest request, StreamObserver<Response> observer) {
        try {
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
                () -> observer.onNext(GetGroupResponse.getDefaultInstance())
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

    private ProtoGroup toProto(ServiceGroup g) {
        return ProtoGroup.newBuilder()
                .setName(g.getName())
                .setServiceType(toProtoType(g.getType()))
                .setTemplateName(g.getTemplateName())
                .setMinOnlineCount(g.getMinServices())
                .setMaxOnlineCount(g.getMaxServices())
                .setMaxPlayers(g.getMaxPlayers())
                .setMemory(g.getMemory())
                .setLifecycle(g.isStatic() ? dev.cloud.proto.common.Lifecycle.LIFECYCLE_STATIC : dev.cloud.proto.common.Lifecycle.LIFECYCLE_DYNAMIC)
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
                p.getMemory(),
                p.getMaxPlayers(),
                p.getMinOnlineCount(),
                p.getMaxOnlineCount(),
                p.getJvmFlags(),
                false
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
}
