package dev.cloud.node.grpc;

import dev.cloud.api.group.ServiceGroup;
import dev.cloud.api.group.ServiceGroupImpl;
import dev.cloud.api.group.ServiceType;
import dev.cloud.proto.service.StartServiceRequest;

/**
 * Maps fields from a gRPC {@link StartServiceRequest} into a {@link ServiceGroup}.
 * The master embeds all necessary group fields in the start request
 * so the node does not need to query them separately.
 */
public class ProtoGroupMapper {

    private ProtoGroupMapper() {
    }

    /**
     * Builds a {@link ServiceGroup} from the fields embedded in a {@link StartServiceRequest}.
     *
     * @param request the incoming start request from the master
     * @return a fully populated {@link ServiceGroup}
     */
    public static ServiceGroup fromStartRequest(StartServiceRequest request) {
        ServiceType type = ServiceType.valueOf(request.getServiceType().name());

        return new ServiceGroupImpl(
                request.getGroupName(),
                type,
                request.getTemplateName(),
                request.getMemoryMb(),
                request.getMaxPlayers(),
                request.getMinServices(),
                request.getMaxServices(),
                request.getJvmFlags(),
                request.isStatic()
        );
    }
}