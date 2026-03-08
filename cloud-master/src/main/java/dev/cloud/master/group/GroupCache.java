package dev.cloud.master.group;

import dev.cloud.api.group.ServiceGroup;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory cache of all service groups.
 * Sits between the {@link MasterGroupManager} and {@link GroupRepository}
 * to avoid disk reads on every lookup.
 */
public class GroupCache {

    private final Map<String, ServiceGroup> cache = new ConcurrentHashMap<>();

    public void put(ServiceGroup group) {
        cache.put(group.getName(), group);
    }

    public void remove(String name) {
        cache.remove(name);
    }

    public Optional<ServiceGroup> get(String name) {
        return Optional.ofNullable(cache.get(name));
    }

    public Collection<ServiceGroup> all() {
        return cache.values();
    }

    public boolean contains(String name) {
        return cache.containsKey(name);
    }

    public int size() {
        return cache.size();
    }
}