package dev.cloud.node.docker;

import dev.cloud.api.group.ServiceGroup;
import dev.cloud.docker.*;
import dev.cloud.node.logging.NodeLogHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Node-level Docker service — creates, starts and stops containers for cloud services.
 * Combines all Docker subsystems into a single high-level API for the service manager.
 */
public class NodeDockerService {

    private static final Logger log = LoggerFactory.getLogger(NodeDockerService.class);

    private final ContainerManager containerManager;
    private final VolumeManager    volumeManager;
    private final NetworkManager   networkManager;
    private final ImageManager     imageManager;
    private final PortAllocator    portAllocator;
    private final ContainerLogStreamer logStreamer;
    private final NodeLogHandler   logHandler;
    private final ImageResolver    imageResolver = new ImageResolver();

    public NodeDockerService(ContainerManager containerManager,
                             VolumeManager volumeManager,
                             NetworkManager networkManager,
                             ImageManager imageManager,
                             PortAllocator portAllocator,
                             ContainerLogStreamer logStreamer,
                             NodeLogHandler logHandler) {
        this.containerManager = containerManager;
        this.volumeManager    = volumeManager;
        this.networkManager   = networkManager;
        this.imageManager     = imageManager;
        this.portAllocator    = portAllocator;
        this.logStreamer       = logStreamer;
        this.logHandler       = logHandler;
    }

    /**
     * Starts a new Docker container for a cloud service.
     *
     * @param serviceName the unique name of the service (e.g. {@code "Lobby-1"})
     * @param group       the service group configuration
     * @param workDir     the host directory mounted into the container as {@code /server}
     * @return the started container's ID and allocated port
     */
    public StartResult start(String serviceName, ServiceGroup group, Path workDir) {
        String image = imageResolver.resolveImage(group.getType());
        imageManager.ensurePresent(image, 120);

        int port = portAllocator.acquire()
                .orElseThrow(() -> new IllegalStateException("No ports available in pool."));

        String jvmFlags = group.getJvmFlags() == null || group.getJvmFlags().isBlank()
                ? imageResolver.resolveDefaultJvmFlags(group.getType(), group.getMemory())
                : group.getJvmFlags();

        String command = jvmFlags + " -jar " + group.getType().getStartCommand();

        String volumeName = volumeManager.createForService(serviceName);

        ContainerBuilder builder = new ContainerBuilder()
                .image(image)
                .name("cloud-" + serviceName)
                .memory(group.getMemory())
                .command("java " + command)
                .port(port, group.getType().getDefaultPort())
                .workingDir("/server")
                .volume(volumeName)
                .env("SERVICE_NAME", serviceName)
                .env("SERVICE_PORT", String.valueOf(port))
                .env("GROUP_NAME", group.getName());

        String containerId = containerManager.createAndStart(builder);
        networkManager.connectContainer(containerId);

        // Stream logs to handler
        Closeable logStream = logStreamer.stream(containerId,
                line -> logHandler.handle(serviceName, line));

        log.info("Service '{}' started in container {} on port {}", serviceName, containerId, port);
        return new StartResult(containerId, port, logStream);
    }

    /**
     * Stops and removes a service container, releases its port and volume.
     *
     * @param serviceName the service name
     * @param containerId the container ID to stop
     * @param port        the port to release back to the pool
     * @param force       if {@code true}, kills immediately instead of graceful stop
     */
    public void stop(String serviceName, String containerId, int port, boolean force) {
        networkManager.disconnectContainer(containerId);
        containerManager.stopAndRemove(containerId, force);
        volumeManager.removeForService(serviceName);
        portAllocator.release(port);
        log.info("Service '{}' stopped and cleaned up.", serviceName);
    }

    /**
     * Returns {@code true} if the container for a service is still running.
     */
    public boolean isRunning(String containerId) {
        return containerManager.isRunning(containerId);
    }

    /**
     * Result of a successful container start.
     */
    public record StartResult(
            String containerId,
            int port,
            Closeable logStream
    ) {}
}