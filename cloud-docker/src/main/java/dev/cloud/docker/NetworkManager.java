package dev.cloud.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Manages a dedicated Docker bridge network for cloud services.
 * All service containers are attached to this network so they can communicate
 * with each other by container name without exposing unnecessary ports.
 */
public class NetworkManager {

    /**
     * Name of the Docker bridge network used by all cloud services.
     */
    public static final String CLOUD_NETWORK = "minecraft-cloud";
    private static final Logger log = LoggerFactory.getLogger(NetworkManager.class);
    private final DockerClient docker;

    public NetworkManager(DockerClient docker) {
        this.docker = docker;
    }

    /**
     * Ensures the cloud network exists, creating it if necessary.
     * Should be called once during node startup.
     *
     * @return the network ID
     */
    public String ensureNetworkExists() {
        Optional<Network> existing = findNetwork(CLOUD_NETWORK);
        if (existing.isPresent()) {
            log.debug("Docker network '{}' already exists.", CLOUD_NETWORK);
            return existing.get().getId();
        }

        String id = docker.createNetworkCmd()
                .withName(CLOUD_NETWORK)
                .withDriver("bridge")
                .withCheckDuplicate(true)
                .exec()
                .getId();

        log.info("Docker network '{}' created with ID: {}", CLOUD_NETWORK, id);
        return id;
    }

    /**
     * Connects a container to the cloud network.
     *
     * @param containerId the container to connect
     */
    public void connectContainer(String containerId) {
        try {
            docker.connectToNetworkCmd()
                    .withNetworkId(CLOUD_NETWORK)
                    .withContainerId(containerId)
                    .exec();
            log.debug("Container {} connected to network '{}'", containerId, CLOUD_NETWORK);
        } catch (Exception e) {
            log.warn("Failed to connect container {} to network '{}': {}",
                    containerId, CLOUD_NETWORK, e.getMessage());
        }
    }

    /**
     * Disconnects a container from the cloud network.
     *
     * @param containerId the container to disconnect
     */
    public void disconnectContainer(String containerId) {
        try {
            docker.disconnectFromNetworkCmd()
                    .withNetworkId(CLOUD_NETWORK)
                    .withContainerId(containerId)
                    .withForce(true)
                    .exec();
            log.debug("Container {} disconnected from network '{}'", containerId, CLOUD_NETWORK);
        } catch (Exception e) {
            log.warn("Failed to disconnect container {} from network '{}': {}",
                    containerId, CLOUD_NETWORK, e.getMessage());
        }
    }

    /**
     * Removes the cloud network. Should only be called during full node shutdown.
     */
    public void removeNetwork() {
        try {
            docker.removeNetworkCmd(CLOUD_NETWORK).exec();
            log.info("Docker network '{}' removed.", CLOUD_NETWORK);
        } catch (NotFoundException e) {
            log.warn("Network '{}' not found when trying to remove.", CLOUD_NETWORK);
        }
    }

    private Optional<Network> findNetwork(String name) {
        List<Network> networks = docker.listNetworksCmd()
                .withNameFilter(name)
                .exec();
        return networks.stream()
                .filter(n -> n.getName().equals(name))
                .findFirst();
    }
}