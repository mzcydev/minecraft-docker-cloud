package dev.cloud.docker;

import dev.cloud.api.group.ServiceType;

/**
 * Resolves the Docker image name and default JVM flags for a given {@link ServiceType}.
 * Centralizes image configuration so it can be changed in one place.
 */
public class ImageResolver {

    /**
     * Returns the Docker image to use for the given service type.
     *
     * @param type the service platform type
     * @return the full Docker image name with tag
     */
    public String resolveImage(ServiceType type) {
        return switch (type) {
            case PAPER, VELOCITY, BUNGEECORD, FABRIC -> "eclipse-temurin:21-jre-alpine";
        };
    }

    /**
     * Returns sensible default JVM flags for the given service type and memory limit.
     * These are used when the group's {@code jvmFlags} field is not explicitly set.
     *
     * @param type     the service type
     * @param memoryMb the RAM limit in megabytes
     * @return a JVM flags string
     */
    public String resolveDefaultJvmFlags(ServiceType type, int memoryMb) {
        String base = "-Xmx" + memoryMb + "M -Xms" + memoryMb + "M";
        String g1gc = " -XX:+UseG1GC -XX:+ParallelRefProcEnabled"
                + " -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions"
                + " -XX:+DisableExplicitGC -XX:G1HeapWastePercent=5";

        return switch (type) {
            case PAPER -> base + g1gc + " -Dfile.encoding=UTF-8 -Dcom.mojang.eula.agree=true";
            case VELOCITY, BUNGEECORD -> base + " -XX:+UseG1GC -Dfile.encoding=UTF-8";
            case FABRIC -> base + g1gc;
        };
    }
}