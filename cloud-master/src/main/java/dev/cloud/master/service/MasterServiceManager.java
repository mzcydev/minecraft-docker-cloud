package dev.cloud.master.service;

import dev.cloud.api.event.EventBus;
import dev.cloud.api.event.events.ServiceStartEvent;
import dev.cloud.api.event.events.ServiceStoppedEvent;
import dev.cloud.api.group.ServiceGroup;
import dev.cloud.api.node.CloudNode;
import dev.cloud.api.service.CloudService;
import dev.cloud.api.service.CloudServiceImpl;
import dev.cloud.api.service.ServiceState;
import dev.cloud.master.MasterConfig;
import dev.cloud.master.node.MasterNodeManager;
import dev.cloud.networking.GrpcChannelManager;
import dev.cloud.networking.service.ServiceRpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Master-side service lifecycle manager.
 * Decides which node to use, sends start/stop commands via gRPC,
 * and tracks all services across the entire cloud.
 */
public class MasterServiceManager {

    private static final Logger log = LoggerFactory.getLogger(MasterServiceManager.class);

    /**
     * serviceId → service
     */
    private final Map<String, CloudServiceImpl> services = new ConcurrentHashMap<>();

    /**
     * groupName → counter for naming (Lobby-1, Lobby-2, ...)
     */
    private final Map<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    private final EventBus eventBus;
    private final MasterNodeManager nodeManager;
    private final GrpcChannelManager channelManager;
    private final MasterConfig config;

    public MasterServiceManager(EventBus eventBus,
                                MasterNodeManager nodeManager,
                                GrpcChannelManager channelManager,
                                MasterConfig config) {
        this.eventBus = eventBus;
        this.nodeManager = nodeManager;
        this.channelManager = channelManager;
        this.config = config;
    }

    /**
     * Starts a new service for the given group on the best available node.
     *
     * @param group the group to start a service for
     * @return the started service, or empty if no node is available
     */
    public Optional<CloudService> startService(ServiceGroup group) {
        Optional<CloudNode> nodeOpt = nodeManager.selectBestNode();
        if (nodeOpt.isEmpty()) {
            log.warn("No node available to start service for group '{}'.", group.getName());
            return Optional.empty();
        }

        CloudNode node = nodeOpt.get();
        String serviceName = nextName(group.getName());
        UUID serviceId = UUID.randomUUID();

        CloudServiceImpl service = new CloudServiceImpl(
                serviceId, serviceName, group.getName(),
                node.getName(), node.getHost(),
                0, group.getMaxPlayers(),
                ServiceState.STARTING, null
        );

        ServiceStartEvent event = new ServiceStartEvent(service);
        eventBus.publish(event);
        if (event.isCancelled()) {
            log.info("ServiceStartEvent cancelled for '{}'.", serviceName);
            return Optional.empty();
        }

        services.put(serviceId.toString(), service);

        // Send start command to the node via gRPC
        try {
            ServiceRpcClient client = new ServiceRpcClient(
                    channelManager.getChannel(node.getName()));
            client.startService(serviceName, group);
            log.info("Start command sent for '{}' → node '{}'", serviceName, node.getName());
        } catch (Exception e) {
            log.error("Failed to send start command for '{}': {}", serviceName, e.getMessage(), e);
            services.remove(serviceId.toString());
            return Optional.empty();
        }

        return Optional.of(service);
    }

    /**
     * Stops a running service by sending a stop command to the node hosting it.
     *
     * @param serviceId the ID of the service to stop
     */
    public void stopService(String serviceId) {
        CloudServiceImpl service = services.remove(serviceId);
        if (service == null) {
            log.warn("Stop requested for unknown service ID: {}", serviceId);
            return;
        }

        try {
            ServiceRpcClient client = new ServiceRpcClient(
                    channelManager.getChannel(service.getNodeName()));
            client.stopService(service.getName());
            log.info("Stop command sent for '{}' → node '{}'", service.getName(), service.getNodeName());
        } catch (Exception e) {
            log.error("Failed to send stop command for '{}': {}", service.getName(), e.getMessage(), e);
        }

        eventBus.publish(new ServiceStoppedEvent(service));
    }

    /**
     * Updates the state of a service.
     * Called by the gRPC {@code ServiceRpcService} when a node reports a state change.
     *
     * @param serviceId the service ID
     * @param state     the new state
     */
    public void updateState(String serviceId, ServiceState state) {
        CloudServiceImpl service = services.get(serviceId);
        if (service != null) {
            service.setState(state);
            log.debug("Service '{}' state → {}", service.getName(), state);
        }
    }

    /**
     * Returns all currently tracked services.
     */
    public Collection<CloudServiceImpl> allServices() {
        return services.values();
    }

    /**
     * Finds a service by ID.
     */
    public Optional<CloudServiceImpl> findById(String serviceId) {
        return Optional.ofNullable(services.get(serviceId));
    }

    /**
     * Finds all services belonging to a group.
     */
    public Collection<CloudServiceImpl> findByGroup(String groupName) {
        return services.values().stream()
                .filter(s -> s.getGroupName().equals(groupName))
                .toList();
    }

    /**
     * Generates the next sequential service name for a group.
     */
    private String nextName(String groupName) {
        int number = counters.computeIfAbsent(groupName, k -> new AtomicInteger(0))
                .incrementAndGet();
        return groupName + "-" + number;
    }
}