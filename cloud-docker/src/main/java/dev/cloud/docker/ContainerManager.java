package dev.cloud.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Container;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Manages the full lifecycle of Docker containers for cloud services.
 * Wraps docker-java calls with logging and error handling.
 */
public class ContainerManager {

    private static final Logger log = LoggerFactory.getLogger(ContainerManager.class);

    /**
     * Seconds to wait for a graceful stop before killing the container.
     */
    private static final int STOP_TIMEOUT_SECONDS = 10;

    private final DockerClient docker;

    /**
     * @param docker the configured Docker client
     */
    public ContainerManager(DockerClient docker) {
        this.docker = docker;
    }

    /**
     * Creates a new container from the given builder configuration.
     *
     * @param builder the configured {@link ContainerBuilder}
     * @return the ID of the created container
     * @throws RuntimeException if container creation fails
     */
    public String create(ContainerBuilder builder) {
        try {
            String id = builder.build(docker).exec().getId();
            log.info("Container created: {}", id);
            return id;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create container: " + e.getMessage(), e);
        }
    }

    /**
     * Starts a previously created container.
     *
     * @param containerId the ID of the container to start
     */
    public void start(String containerId) {
        try {
            docker.startContainerCmd(containerId).exec();
            log.info("Container started: {}", containerId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to start container " + containerId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Convenience method that creates and immediately starts a container.
     *
     * @param builder the configured {@link ContainerBuilder}
     * @return the ID of the started container
     */
    public String createAndStart(ContainerBuilder builder) {
        String id = create(builder);
        start(id);
        return id;
    }

    /**
     * Gracefully stops a running container.
     * Waits up to {@value #STOP_TIMEOUT_SECONDS} seconds before killing it.
     *
     * @param containerId the ID of the container to stop
     */
    public void stop(String containerId) {
        try {
            docker.stopContainerCmd(containerId)
                    .withTimeout(STOP_TIMEOUT_SECONDS)
                    .exec();
            log.info("Container stopped: {}", containerId);
        } catch (NotFoundException e) {
            log.warn("Container {} not found when trying to stop.", containerId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to stop container " + containerId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Immediately kills a running container without waiting for graceful shutdown.
     *
     * @param containerId the ID of the container to kill
     */
    public void kill(String containerId) {
        try {
            docker.killContainerCmd(containerId).exec();
            log.info("Container killed: {}", containerId);
        } catch (NotFoundException e) {
            log.warn("Container {} not found when trying to kill.", containerId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to kill container " + containerId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Removes a stopped container and its anonymous volumes.
     *
     * @param containerId the ID of the container to remove
     */
    public void remove(String containerId) {
        try {
            docker.removeContainerCmd(containerId)
                    .withRemoveVolumes(true)
                    .withForce(false)
                    .exec();
            log.info("Container removed: {}", containerId);
        } catch (NotFoundException e) {
            log.warn("Container {} not found when trying to remove.", containerId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to remove container " + containerId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Stops and removes a container in one step.
     *
     * @param containerId the ID of the container
     * @param force       if {@code true}, kills the container immediately instead of stopping gracefully
     */
    public void stopAndRemove(String containerId, boolean force) {
        if (force) {
            kill(containerId);
        } else {
            stop(containerId);
        }
        remove(containerId);
    }

    /**
     * Restarts a running container.
     *
     * @param containerId the ID of the container to restart
     */
    public void restart(String containerId) {
        try {
            docker.restartContainerCmd(containerId).exec();
            log.info("Container restarted: {}", containerId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to restart container " + containerId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Checks whether a container with the given ID is currently running.
     *
     * @param containerId the container ID to check
     * @return {@code true} if the container is in "running" state
     */
    public boolean isRunning(String containerId) {
        try {
            var info = docker.inspectContainerCmd(containerId).exec();
            return Boolean.TRUE.equals(info.getState().getRunning());
        } catch (NotFoundException e) {
            return false;
        }
    }

    /**
     * Lists all containers currently managed by this Docker daemon.
     *
     * @param all if {@code true}, includes stopped containers; otherwise only running ones
     * @return list of containers
     */
    public List<Container> listContainers(boolean all) {
        return docker.listContainersCmd()
                .withShowAll(all)
                .exec();
    }

    /**
     * Finds a container by name.
     *
     * @param name the container name to search for (exact match)
     * @return an {@link Optional} containing the container, or empty if not found
     */
    public Optional<Container> findByName(String name) {
        return listContainers(true).stream()
                .filter(c -> List.of(c.getNames()).contains("/" + name))
                .findFirst();
    }
}