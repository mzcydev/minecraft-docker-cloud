package dev.cloud.api.service;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Utility class providing common filtering and selection operations on collections of {@link CloudService}.
 */
public final class ServiceFilter {

    private ServiceFilter() {
    }

    /**
     * Returns all services that are currently in {@link ServiceState#ONLINE} state.
     */
    public static List<CloudService> online(Collection<CloudService> services) {
        return services.stream().filter(CloudService::isOnline).toList();
    }

    /**
     * Returns all services that have not yet reached their player capacity.
     */
    public static List<CloudService> notFull(Collection<CloudService> services) {
        return services.stream().filter(s -> !s.isFull()).toList();
    }

    /**
     * Returns all services belonging to the specified group.
     *
     * @param groupName the group name to filter by (case-insensitive)
     */
    public static List<CloudService> byGroup(Collection<CloudService> services, String groupName) {
        return services.stream()
                .filter(s -> s.getGroup().getName().equalsIgnoreCase(groupName))
                .toList();
    }

    /**
     * Selects the online, non-full service with the fewest connected players.
     * Useful for lobby balancing.
     *
     * @return the least populated service, or empty if none is available
     */
    public static Optional<CloudService> leastPlayers(Collection<CloudService> services) {
        return services.stream()
                .filter(CloudService::isOnline)
                .filter(s -> !s.isFull())
                .min(Comparator.comparingInt(CloudService::getOnlineCount));
    }

    /**
     * Selects a random online, non-full service from the collection.
     *
     * @return a randomly chosen service, or empty if none is available
     */
    public static Optional<CloudService> random(Collection<CloudService> services) {
        var available = notFull(online(services));
        if (available.isEmpty()) return Optional.empty();
        return Optional.of(available.get((int) (Math.random() * available.size())));
    }
}