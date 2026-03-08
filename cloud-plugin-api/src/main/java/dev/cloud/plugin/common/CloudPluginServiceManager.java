package dev.cloud.plugin.common;

import dev.cloud.api.service.CloudService;
import dev.cloud.networking.group.GroupRpcClient;
import dev.cloud.networking.service.ServiceRpcClient;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Plugin-side service manager.
 * Caches the list of running services and provides lookup methods.
 * Syncs with the master periodically via gRPC.
 */
public class CloudPluginServiceManager {

    private static final Logger log = LoggerFactory.getLogger(CloudPluginServiceManager.class);

    private final ServiceRpcClient serviceClient;
    private final GroupRpcClient groupClient;
    private final CloudPluginEventBus eventBus;

    private final List<CloudService> services = new CopyOnWriteArrayList<>();

    public CloudPluginServiceManager(ManagedChannel channel, CloudPluginEventBus eventBus) {
        this.serviceClient = new ServiceRpcClient(channel);
        this.groupClient   = new GroupRpcClient(channel);
        this.eventBus      = eventBus;
    }

    /**
     * Finds a service by name from the local cache.
     *
     * @param name the service name
     * @return an {@link Optional} containing the service, or empty if not found
     */
    public Optional<CloudService> findByName(String name) {
        return services.stream()
                .filter(s -> s.getName().equals(name))
                .findFirst();
    }

    /**
     * Returns all services in the given group from the local cache.
     *
     * @param groupName the group name to filter by
     */
    public List<CloudService> getByGroup(String groupName) {
        return services.stream()
                .filter(s -> s.getGroupName().equals(groupName))
                .toList();
    }

    /**
     * Returns all cached services.
     */
    public List<CloudService> all() {
        return List.copyOf(services);
    }

    /**
     * Updates the local service cache.
     * Called when a service state change event is received.
     *
     * @param service the updated service
     */
    public void updateCache(CloudService service) {
        services.removeIf(s -> s.getName().equals(service.getName()));
        services.add(service);
    }

    /**
     * Removes a service from the local cache.
     *
     * @param serviceName the name of the service to remove
     */
    public void removeFromCache(String serviceName) {
        services.removeIf(s -> s.getName().equals(serviceName));
    }
}