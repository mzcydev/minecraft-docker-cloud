package dev.cloud.api.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates sequential, human-readable service names like {@code "Lobby-1"}, {@code "BedWars-3"}.
 * Counters are maintained per group name and are thread-safe.
 */
public class NameGenerator {

    private final Map<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    /**
     * Returns the next name for the given group (e.g. {@code "Lobby-1"}, then {@code "Lobby-2"}).
     *
     * @param groupName the name of the group
     * @return the next sequential service name
     */
    public String next(String groupName) {
        int number = counters
                .computeIfAbsent(groupName, k -> new AtomicInteger(0))
                .incrementAndGet();
        return groupName + "-" + number;
    }

    /**
     * Resets the counter for the given group back to zero.
     *
     * @param groupName the group whose counter should be reset
     */
    public void reset(String groupName) {
        counters.remove(groupName);
    }
}