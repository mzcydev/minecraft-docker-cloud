package dev.cloud.rest.mapper;

import dev.cloud.api.group.ServiceGroup;
import dev.cloud.api.group.ServiceGroupImpl;
import dev.cloud.api.group.ServiceType;
import dev.cloud.rest.dto.GroupDto;

/**
 * Maps between {@link ServiceGroup} domain objects and {@link GroupDto} REST DTOs.
 */
public class GroupMapper {

    public GroupDto toDto(ServiceGroup group) {
        return new GroupDto(
                group.getName(),
                group.getType().name(),
                group.getTemplateName(),
                group.getMemory(),
                group.getMaxPlayers(),
                group.getMinServices(),
                group.getMaxServices(),
                group.getJvmFlags(),
                group.isStatic()
        );
    }

    public ServiceGroup toDomain(GroupDto dto) {
        return new ServiceGroupImpl(
                dto.name(),
                ServiceType.valueOf(dto.type().toUpperCase()),
                dto.templateName(),
                dto.memory(),
                dto.maxPlayers(),
                dto.minServices(),
                dto.maxServices(),
                dto.jvmFlags(),
                dto.isStatic()
        );
    }
}