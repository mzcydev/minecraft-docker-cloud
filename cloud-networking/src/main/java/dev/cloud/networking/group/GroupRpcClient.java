package dev.cloud.networking.group;

import dev.cloud.proto.common.Empty;
import dev.cloud.proto.group.GetGroupRequest;
import dev.cloud.proto.group.GroupServiceGrpc;
import dev.cloud.proto.group.ProtoGroup;
import io.grpc.ManagedChannel;

import java.util.List;

/**
 * Stub wrapper for calling the master's {@link GroupServiceGrpc} from a node or plugin.
 * Nodes use this to fetch group configurations on startup.
 */
public class GroupRpcClient {

    private final GroupServiceGrpc.GroupServiceBlockingStub stub;

    /**
     * @param channel the open channel to the master
     */
    public GroupRpcClient(ManagedChannel channel) {
        this.stub = GroupServiceGrpc.newBlockingStub(channel);
    }

    /**
     * Returns all registered groups from the master.
     */
    public List<ProtoGroup> listGroups() {
        return stub.listGroups(Empty.getDefaultInstance()).getGroupsList();
    }

    /**
     * Returns the configuration of a single group by name.
     *
     * @param name the group name
     * @return the group proto, or a default (empty) instance if not found
     */
    public ProtoGroup getGroup(String name) {
        return stub.getGroup(GetGroupRequest.newBuilder().setName(name).build()).getGroup();
    }
}