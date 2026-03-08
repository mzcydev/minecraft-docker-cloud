package dev.cloud.api.service;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages the lifecycle of all {@link CloudService} instances across the cluster.
 */
public interface ServiceManager {

    /**
     * Starts a single new service for the given group.
     *
     * @param groupName the name of the group to start a service for
     */
    void startService(String groupName);

    /**
     * Starts {@code count} new services for the given group.
     *
     * @param groupName the name of the group to start services for
     * @param count     number of services to start
     */
    void startService(String groupName, int count);

    /**
     * Gracefully stops the service with the given name.
     *
     * @param serviceName the name of the service to stop (e.g. "Lobby-1")
     */
    void stopService(String serviceName);

    /**
     * Gracefully stops the service with the given unique ID.
     *
     * @param uniqueId the unique ID of the service to stop
     */
    void stopService(UUID uniqueId);

    /**
     * Stops and immediately restarts the service with the given name.
     *
     * @param serviceName the name of the service to restart
     */
    void restartService(String serviceName);

    /**
     * Looks up a service by its display name.
     *
     * @param name the service name (e.g. "BedWars-2")
     * @return an {@link Optional} containing the service, or empty if not found
     */
    Optional<CloudService> getService(String name);

    /**
     * Looks up a service by its unique ID.
     *
     * @param uniqueId the UUID of the service
     * @return an {@link Optional} containing the service, or empty if not found
     */
    Optional<CloudService> getService(UUID uniqueId);

    /**
     * Returns all known services across all groups and nodes.
     */
    Collection<CloudService> getAllServices();

    /**
     * Returns all services belonging to the given group.
     *
     * @param groupName the group name to filter by
     */
    Collection<CloudService> getServicesByGroup(String groupName);

    /**
     * Returns all services currently in the given state.
     *
     * @param state the state to filter by
     */
    Collection<CloudService> getServicesByState(ServiceState state);

    /**
     * Returns the total number of players connected across all online services.
     */
    int getOnlineCount();
}