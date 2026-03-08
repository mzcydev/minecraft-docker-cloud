package dev.cloud.rest.dto;

/**
 * Data Transfer Object for service status responses.
 */
public record ServiceDto(
        String id,
        String name,
        String groupName,
        String nodeName,
        String host,
        int port,
        int onlinePlayers,
        int maxPlayers,
        String state
) {
}