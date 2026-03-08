package dev.cloud.rest.mapper;

import dev.cloud.api.service.CloudServiceImpl;
import dev.cloud.rest.dto.ServiceDto;

/**
 * Maps between {@link CloudServiceImpl} domain objects and {@link ServiceDto} REST DTOs.
 */
public class ServiceMapper {

    public ServiceDto toDto(CloudServiceImpl service) {
        return new ServiceDto(
                service.getId().toString(),
                service.getName(),
                service.getGroupName(),
                service.getNodeName(),
                service.getHost(),
                service.getPort(),
                service.getOnlinePlayers(),
                service.getMaxPlayers(),
                service.getState().name()
        );
    }
}