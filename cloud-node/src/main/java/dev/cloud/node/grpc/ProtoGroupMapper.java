package dev.cloud.node.grpc;

import dev.cloud.api.group.ServiceGroup;
import dev.cloud.api.group.ServiceGroupImpl;
import dev.cloud.api.group.ServiceType;
import dev.cloud.proto.service.StartServiceRequest;

public class ProtoGroupMapper {

    private ProtoGroupMapper() {}

    public static ServiceGroup fromStartRequest(StartServiceRequest request) {
        // ServiceType comes via service_type field in proto (may be unset → default PAPER)
        ServiceType type;
        try {
            type = ServiceType.valueOf(
                request.getServiceType().name().replace("SERVICE_TYPE_", "")
            );
        } catch (IllegalArgumentException e) {
            type = ServiceType.PAPER;
        }

        return new ServiceGroupImpl(
                request.getGroupName(),
                type,
                request.getTemplateName(),
                request.getMemory(),   // memory field from proto
                100,                   // maxPlayers - not in proto, use default
                1,                     // minServices - not in proto
                10,                    // maxServices - not in proto
                request.getJvmFlags(),
                request.getLifecycle().name().contains("STATIC")
        );
    }
}
