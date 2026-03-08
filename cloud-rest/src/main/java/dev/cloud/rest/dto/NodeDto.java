package dev.cloud.rest.dto;

/**
 * Data Transfer Object for node status responses.
 */
public record NodeDto(
        String name,
        String host,
        String state,
        long usedMemoryMb,
        int maxMemoryMb,
        int runningServices,
        int maxServices
) {
}