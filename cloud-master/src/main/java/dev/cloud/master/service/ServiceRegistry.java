package dev.cloud.master.service;

import dev.cloud.api.service.CloudServiceImpl;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central in-memory registry of all services currently running across the cloud.
 */
public class ServiceRegistry {

    /**
     * serviceId → service
     */
    private final Map<String, CloudServiceImpl> byId = new ConcurrentHashMap<>();
    private final Map<String, CloudServiceImpl> byName = new ConcurrentHashMap<>();

    public void add(CloudServiceImpl service) {
        byId.put(service.getId().toString(), service);
        byName.put(service.getName(), service);
    }

    public void remove(String serviceId) {
        CloudServiceImpl s = byId.remove(serviceId);
        if (s != null) byName.remove(s.getName());
    }

    public Optional<CloudServiceImpl> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    public Optional<CloudServiceImpl> findByName(String name) {
        return Optional.ofNullable(byName.get(name));
    }

    public Collection<CloudServiceImpl> allServices() {
        return byId.values();
    }

    public Collection<CloudServiceImpl> findByGroup(String groupName) {
        return byId.values().stream()
                .filter(s -> s.getGroupName().equals(groupName))
                .toList();
    }
}