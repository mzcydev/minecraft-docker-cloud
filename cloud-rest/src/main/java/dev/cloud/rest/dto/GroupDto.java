package dev.cloud.rest.dto;

/**
 * Data Transfer Object for service group responses and creation requests.
 */
public record GroupDto(
        String name,
        String type,
        String templateName,
        int memory,
        int maxPlayers,
        int minServices,
        int maxServices,
        String jvmFlags,
        boolean isStatic
) {}