package dev.cloud.master.node;

import dev.cloud.api.node.CloudNodeImpl;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central in-memory registry of all nodes connected to the master.
 * Acts as the single source of truth for node state.
 */
public class NodeRegistry {

    private final Map<String, CloudNodeImpl> nodes = new ConcurrentHashMap<>();

    public void add(CloudNodeImpl node) {
        nodes.put(node.getName(), node);
    }

    public void remove(String name) {
        nodes.remove(name);
    }

    public Optional<CloudNodeImpl> findNode(String name) {
        return Optional.ofNullable(nodes.get(name));
    }

    public Collection<CloudNodeImpl> allNodes() {
        return nodes.values();
    }

    public int count() {
        return nodes.size();
    }

    public boolean contains(String name) {
        return nodes.containsKey(name);
    }
}