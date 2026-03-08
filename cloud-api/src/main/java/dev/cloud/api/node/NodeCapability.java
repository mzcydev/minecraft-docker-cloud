package dev.cloud.api.node;

/**
 * Describes the static hardware capabilities of a node as reported at registration time.
 */
public record NodeCapability(
        int totalMemoryMb,
        int cpuCores,
        String operatingSystem,
        String javaVersion,
        String dockerVersion
) {
}