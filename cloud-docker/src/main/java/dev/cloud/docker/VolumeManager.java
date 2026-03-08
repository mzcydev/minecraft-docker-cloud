package dev.cloud.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages named Docker volumes used to persist service data (worlds, plugins, configs).
 * Each service gets its own volume named after the service (e.g. {@code "cloud-Lobby-1"}).
 */
public class VolumeManager {

    private static final Logger log = LoggerFactory.getLogger(VolumeManager.class);

    private static final String VOLUME_PREFIX = "cloud-";

    private final DockerClient docker;

    public VolumeManager(DockerClient docker) {
        this.docker = docker;
    }

    /**
     * Returns the volume name for a given service name.
     *
     * @param serviceName the service name
     * @return the Docker volume name
     */
    public static String volumeNameFor(String serviceName) {
        return VOLUME_PREFIX + serviceName;
    }

    /**
     * Creates a named volume for the given service if it does not already exist.
     *
     * @param serviceName the service name (e.g. {@code "Lobby-1"})
     * @return the volume name (e.g. {@code "cloud-Lobby-1"})
     */
    public String createForService(String serviceName) {
        String volumeName = VOLUME_PREFIX + serviceName;
        try {
            docker.createVolumeCmd().withName(volumeName).exec();
            log.info("Volume created: {}", volumeName);
        } catch (Exception e) {
            log.warn("Volume '{}' may already exist: {}", volumeName, e.getMessage());
        }
        return volumeName;
    }

    /**
     * Removes the volume associated with the given service.
     * Should be called after the service container has been removed.
     *
     * @param serviceName the service name whose volume should be removed
     */
    public void removeForService(String serviceName) {
        String volumeName = VOLUME_PREFIX + serviceName;
        try {
            docker.removeVolumeCmd(volumeName).exec();
            log.info("Volume removed: {}", volumeName);
        } catch (NotFoundException e) {
            log.warn("Volume '{}' not found when trying to remove.", volumeName);
        } catch (Exception e) {
            log.error("Failed to remove volume '{}': {}", volumeName, e.getMessage());
        }
    }

    /**
     * Returns {@code true} if a volume with the given service name exists.
     *
     * @param serviceName the service name to check
     */
    public boolean existsForService(String serviceName) {
        String volumeName = VOLUME_PREFIX + serviceName;
        try {
            docker.inspectVolumeCmd(volumeName).exec();
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }
}