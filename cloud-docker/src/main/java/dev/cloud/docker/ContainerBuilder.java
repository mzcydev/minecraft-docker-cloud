package dev.cloud.docker;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Volume;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fluent builder for Docker container configurations.
 * Wraps docker-java's {@link CreateContainerCmd} with a cleaner API
 * tailored to Minecraft server containers.
 */
public class ContainerBuilder {

    private final List<String> command = new ArrayList<>();
    private final Map<String, String> envVars = new HashMap<>();
    private final Map<Integer, Integer> portBindings = new HashMap<>(); // hostPort -> containerPort
    private String image;
    private String name;
    private int memoryMb;
    private String workingDir = "/server";
    private String volumeName;

    /**
     * Sets the Docker image to use.
     *
     * @param image the full image name (e.g. {@code "eclipse-temurin:21-jre-alpine"})
     */
    public ContainerBuilder image(String image) {
        this.image = image;
        return this;
    }

    /**
     * Sets the container name (must be unique on the host).
     *
     * @param name the container name (e.g. {@code "cloud-Lobby-1"})
     */
    public ContainerBuilder name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the memory limit for the container.
     *
     * @param memoryMb the memory limit in megabytes
     */
    public ContainerBuilder memory(int memoryMb) {
        this.memoryMb = memoryMb;
        return this;
    }

    /**
     * Sets the command to run inside the container (split by spaces).
     *
     * @param cmd the full command string (e.g. {@code "java -Xmx512M -jar server.jar"})
     */
    public ContainerBuilder command(String cmd) {
        this.command.clear();
        this.command.addAll(List.of(cmd.split(" ")));
        return this;
    }

    /**
     * Adds an environment variable to the container.
     *
     * @param key   the variable name
     * @param value the variable value
     */
    public ContainerBuilder env(String key, String value) {
        this.envVars.put(key, value);
        return this;
    }

    /**
     * Adds a port binding from the host to the container.
     *
     * @param hostPort      the port on the host machine
     * @param containerPort the port inside the container
     */
    public ContainerBuilder port(int hostPort, int containerPort) {
        this.portBindings.put(hostPort, containerPort);
        return this;
    }

    /**
     * Sets the working directory inside the container.
     *
     * @param dir the working directory path (default: {@code "/server"})
     */
    public ContainerBuilder workingDir(String dir) {
        this.workingDir = dir;
        return this;
    }

    /**
     * Mounts a named Docker volume into the container at {@code /server}.
     *
     * @param volumeName the name of the Docker volume
     */
    public ContainerBuilder volume(String volumeName) {
        this.volumeName = volumeName;
        return this;
    }

    /**
     * Builds and returns the docker-java {@link CreateContainerCmd}.
     *
     * @param client the Docker client to create the command from
     * @return the configured create command, ready to be executed
     * @throws IllegalStateException if required fields (image, name) are missing
     */
    public CreateContainerCmd build(com.github.dockerjava.api.DockerClient client) {
        if (image == null || image.isBlank()) throw new IllegalStateException("Container image must be set.");
        if (name == null || name.isBlank()) throw new IllegalStateException("Container name must be set.");

        CreateContainerCmd cmd = client.createContainerCmd(image)
                .withName(name)
                .withWorkingDir(workingDir);

        // Memory limit
        if (memoryMb > 0) {
            cmd.withHostConfig(com.github.dockerjava.api.model.HostConfig.newHostConfig()
                    .withMemory((long) memoryMb * 1024 * 1024)
                    .withMemorySwap((long) memoryMb * 1024 * 1024) // disable swap
                    .withPortBindings(buildPortBindings())
                    .withBinds(buildBinds())
            );
        }

        // Command
        if (!command.isEmpty()) {
            cmd.withCmd(command);
        }

        // Env vars
        if (!envVars.isEmpty()) {
            List<String> env = envVars.entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .toList();
            cmd.withEnv(env);
        }

        // Exposed ports
        if (!portBindings.isEmpty()) {
            ExposedPort[] exposed = portBindings.values().stream()
                    .map(ExposedPort::tcp)
                    .toArray(ExposedPort[]::new);
            cmd.withExposedPorts(exposed);
        }

        return cmd;
    }

    private Ports buildPortBindings() {
        Ports ports = new Ports();
        portBindings.forEach((hostPort, containerPort) ->
                ports.bind(ExposedPort.tcp(containerPort), Ports.Binding.bindPort(hostPort))
        );
        return ports;
    }

    private List<Bind> buildBinds() {
        List<Bind> binds = new ArrayList<>();
        if (volumeName != null) {
            binds.add(new Bind(volumeName, new Volume(workingDir)));
        }
        return binds;
    }
}