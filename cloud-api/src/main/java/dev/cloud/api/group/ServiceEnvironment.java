package dev.cloud.api.group;

/**
 * Maps a {@link ServiceType} to the Docker image and start command used to run it.
 * The placeholder {@code %jvm_flags%} is replaced at runtime with the group's JVM flags.
 */
public record ServiceEnvironment(
        ServiceType type,
        String dockerImage,
        String startCommand
) {
    /**
     * Returns the default environment configuration for the given service type.
     *
     * @param type the service type to resolve
     * @return the matching {@link ServiceEnvironment}
     */
    public static ServiceEnvironment of(ServiceType type) {
        return switch (type) {
            case PAPER -> new ServiceEnvironment(type,
                    "eclipse-temurin:21-jre-alpine",
                    "java %jvm_flags% -jar server.jar --nogui");
            case VELOCITY -> new ServiceEnvironment(type,
                    "eclipse-temurin:21-jre-alpine",
                    "java %jvm_flags% -jar velocity.jar");
            case BUNGEECORD -> new ServiceEnvironment(type,
                    "eclipse-temurin:21-jre-alpine",
                    "java %jvm_flags% -jar bungeecord.jar");
            case FABRIC -> new ServiceEnvironment(type,
                    "eclipse-temurin:21-jre-alpine",
                    "java %jvm_flags% -jar fabric-server-launch.jar nogui");
        };
    }

    /**
     * Returns the start command with {@code %jvm_flags%} replaced by the given flags string.
     *
     * @param jvmFlags the JVM flags to inject (e.g. {@code "-Xmx512M -XX:+UseG1GC"})
     * @return the resolved start command
     */
    public String resolveStartCommand(String jvmFlags) {
        return startCommand.replace("%jvm_flags%", jvmFlags);
    }
}