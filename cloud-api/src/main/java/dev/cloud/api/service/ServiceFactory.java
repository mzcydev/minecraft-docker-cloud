package dev.cloud.api.service;

import dev.cloud.api.group.ServiceGroup;
import dev.cloud.api.node.CloudNode;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Creates new {@link ServiceSnapshot} instances from a group definition and a target node.
 * Keeps per-group counters to generate sequential service names (e.g. "Lobby-1", "Lobby-2").
 */
public class ServiceFactory {

    /** Per-group counter for sequential service numbering. */
    private final Map<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    /**
     * Creates a new service snapshot in {@link ServiceState#PREPARED} state.
     *
     * @param group the group this service belongs to
     * @param node  the node the service will run on
     * @param port  the port assigned to this service
     * @return a new immutable {@link ServiceSnapshot}
     */
    public ServiceSnapshot create(ServiceGroup group, CloudNode node, int port) {
        int number = counters
                .computeIfAbsent(group.getName(), k -> new AtomicInteger(0))
                .incrementAndGet();

        String name = group.getName() + "-" + number;

        return new ServiceSnapshot(
                UUID.randomUUID(),
                name,
                group.getName(),
                node.getName(),
                ServiceState.PREPARED,
                group.getLifecycle(),
                port,
                0,
                group.getMaxPlayers(),
                ""
        );
    }

    /**
     * Resets the sequential counter for the given group.
     * Should be called when a group is deleted or the master restarts.
     *
     * @param groupName the group whose counter should be reset
     */
    public void resetCounter(String groupName) {
        counters.remove(groupName);
    }
}