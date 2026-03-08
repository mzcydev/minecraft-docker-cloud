package dev.cloud.rest.mapper;

import dev.cloud.api.node.CloudNodeImpl;
import dev.cloud.rest.dto.NodeDto;

/**
 * Maps between {@link CloudNodeImpl} domain objects and {@link NodeDto} REST DTOs.
 */
public class NodeMapper {

    public NodeDto toDto(CloudNodeImpl node) {
        return new NodeDto(
                node.getName(),
                node.getHost(),
                node.getState().name(),
                node.getUsedMemoryMb(),
                node.getMaxMemoryMb(),
                node.getRunningServices(),
                node.getMaxServices()
        );
    }
}