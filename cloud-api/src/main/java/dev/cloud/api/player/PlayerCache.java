package dev.cloud.api.player;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory cache mapping UUIDs and usernames to {@link CloudPlayer} instances.
 */
public class PlayerCache {

    private final ConcurrentHashMap<UUID, CloudPlayer> byUuid = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, UUID> byName = new ConcurrentHashMap<>();

    /**
     * Adds a player to the cache.
     *
     * @param player the player to cache
     */
    public void add(CloudPlayer player) {
        byUuid.put(player.getUniqueId(), player);
        byName.put(player.getName().toLowerCase(), player.getUniqueId());
    }

    /**
     * Removes a player from the cache by UUID.
     *
     * @param uniqueId the UUID of the player to remove
     */
    public void remove(UUID uniqueId) {
        CloudPlayer player = byUuid.remove(uniqueId);
        if (player != null) byName.remove(player.getName().toLowerCase());
    }

    /**
     * Looks up a player by UUID.
     *
     * @param uniqueId the player's UUID
     * @return an {@link Optional} with the player, or empty if not cached
     */
    public Optional<CloudPlayer> get(UUID uniqueId) {
        return Optional.ofNullable(byUuid.get(uniqueId));
    }

    /**
     * Looks up a player by username (case-insensitive).
     *
     * @param name the player's username
     * @return an {@link Optional} with the player, or empty if not cached
     */
    public Optional<CloudPlayer> get(String name) {
        UUID id = byName.get(name.toLowerCase());
        return id == null ? Optional.empty() : Optional.ofNullable(byUuid.get(id));
    }

    /**
     * Returns all cached players as an unmodifiable collection.
     */
    public Collection<CloudPlayer> getAll() {
        return Collections.unmodifiableCollection(byUuid.values());
    }

    /**
     * Returns the number of players currently in the cache.
     */
    public int size() {
        return byUuid.size();
    }
}