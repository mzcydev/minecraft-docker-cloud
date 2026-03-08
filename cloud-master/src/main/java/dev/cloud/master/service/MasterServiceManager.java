package dev.cloud.master.service;

import dev.cloud.api.event.EventBus;
import dev.cloud.api.event.events.service.ServiceStartEvent;
import dev.cloud.api.event.events.service.ServiceStoppedEvent;
import dev.cloud.api.group.ServiceGroup;
import dev.cloud.api.node.CloudNode;
import dev.cloud.api.service.CloudService;
import dev.cloud.api.service.CloudServiceImpl;
import dev.cloud.api.service.ServiceManager;
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

public class MasterServiceManager implements ServiceManager {

    private static final Logger log = LoggerFactory.getLogger(MasterServiceManager.class);

    private final Map<String, CloudServiceImpl> byId   = new ConcurrentHashMap<>();
    private final Map<String, CloudServiceImpl> byName = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> counters  = new ConcurrentHashMap<>();

    private final EventBus eventBus;
    private final MasterNodeManager nodeManager;
    private final GrpcChannelManager channelManager;
    private final MasterConfig config;

    public MasterServiceManager(EventBus eventBus, MasterNodeManager nodeManager,
                                GrpcChannelManager channelManager, MasterConfig config) {
        this.eventBus       = eventBus;
        this.nodeManager    = nodeManager;
        this.channelManager = channelManager;
        this.config         = config;
    }

    public Optional<CloudService> startService(ServiceGroup group) {
        Optional<CloudNode> nodeOpt = nodeManager.selectBestNode(group.getMemory());
        if (nodeOpt.isEmpty()) {
            log.warn("No node available for group '{}'.", group.getName());
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

        byId.put(serviceId.toString(), service);
        byName.put(serviceName, service);

        try {
            ServiceRpcClient client = new ServiceRpcClient(channelManager.getChannel(node.getName()));
            client.startService(serviceName, group);
            log.info("Start command sent: '{}' → node '{}'", serviceName, node.getName());
        } catch (Exception e) {
            log.error("Failed to send start for '{}': {}", serviceName, e.getMessage(), e);
            byId.remove(serviceId.toString());
            byName.remove(serviceName);
            return Optional.empty();
        }

        return Optional.of(service);
    }

    public void stopService(String serviceId) {
        CloudServiceImpl service = byId.remove(serviceId);
        if (service == null) { log.warn("Stop requested for unknown ID: {}", serviceId); return; }
        byName.remove(service.getName());

        try {
            ServiceRpcClient client = new ServiceRpcClient(channelManager.getChannel(service.getNodeName()));
            client.stopService(service.getName());
        } catch (Exception e) {
            log.error("Failed to send stop for '{}': {}", service.getName(), e.getMessage(), e);
        }

        eventBus.publish(new ServiceStoppedEvent(service));
    }

    @Override
    public void updateState(String serviceId, ServiceState state) {
        CloudServiceImpl s = byName.get(serviceName);
        if (s != null) { s.setState(state); log.debug("Service '{}' → {}", s.getName(), state); }
    }

    @Override
    public void updatePlayerCount(String serviceId, int count) {
        CloudServiceImpl s = byName.get(serviceName);
        if (s != null) s.setOnlinePlayers(count);
    }

    @Override
    public Optional<CloudService> getService(String name) {
        return Optional.ofNullable(byName.get(name));
    }

    @Override
    public Collection<CloudService> getAllServices() {
        return byId.values().stream().map(s -> (CloudService) s).toList();
    }

    @Override
    public Collection<CloudService> getServicesByGroup(String groupName) {
        return byId.values().stream()
                .filter(s -> s.getGroupName().equals(groupName))
                .map(s -> (CloudService) s)
                .toList();
    }

    public Optional<CloudServiceImpl> findById(String id) { return Optional.ofNullable(byId.get(id)); }

    public Collection<CloudServiceImpl> allServices() { return byId.values(); }

    public Collection<CloudServiceImpl> findByGroup(String groupName) {
        return byId.values().stream().filter(s -> s.getGroupName().equals(groupName)).toList();
    }

    private String nextName(String groupName) {
        return groupName + "-" + counters.computeIfAbsent(groupName, k -> new AtomicInteger(0)).incrementAndGet();
    }
}
