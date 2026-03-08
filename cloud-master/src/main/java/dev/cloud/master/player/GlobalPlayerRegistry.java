package dev.cloud.master.player;

import dev.cloud.api.player.CloudPlayerImpl;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global in-memory registry of all players currently on the network.
 * Backed by two maps for O(1) lookup by UUID and by name.
 */
public class GlobalPlayerRegistry {

    private final Map<UUID, CloudPlayerImpl> byUuid = new ConcurrentHashMap<>();
    private final Map<String, CloudPlayerImpl> byName = new ConcurrentHashMap<>();

    public void add(CloudPlayerImpl player) {
        byUuid.put(player.getUuid(), player);
        byName.put(player.getName().toLowerCase(), player);
    }

    public void remove(UUID uuid) {
        CloudPlayerImpl p = byUuid.remove(uuid);
        if (p != null) byName.remove(p.getName().toLowerCase());
    }

    public Optional<CloudPlayerImpl> findByUuid(UUID uuid) {
        return Optional.ofNullable(byUuid.get(uuid));
    }

    public Optional<CloudPlayerImpl> findByName(String name) {
        return Optional.ofNullable(byName.get(name.toLowerCase()));
    }

    public Collection<CloudPlayerImpl> allPlayers() {
        return byUuid.values();
    }

    public int count() {
        return byUuid.size();
    }
}