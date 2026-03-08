package dev.cloud.master.group;

import dev.cloud.api.event.EventBus;
import dev.cloud.api.event.events.GroupCreateEvent;
import dev.cloud.api.event.events.GroupDeleteEvent;
import dev.cloud.api.event.events.GroupUpdateEvent;
import dev.cloud.api.group.GroupManager;
import dev.cloud.api.group.ServiceGroup;
import dev.cloud.api.group.ServiceGroupImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages service groups on the master.
 * Groups are stored in memory and persisted as JSON files under {@code groups/}.
 */
public class MasterGroupManager implements GroupManager {

    private static final Logger log = LoggerFactory.getLogger(MasterGroupManager.class);
    private static final Path GROUP_DIR = Path.of("groups");
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private final Map<String, ServiceGroup> groups = new ConcurrentHashMap<>();
    private final EventBus eventBus;

    public MasterGroupManager(EventBus eventBus) {
        this.eventBus = eventBus;
        loadFromDisk();
    }

    @Override
    public void createGroup(ServiceGroup group) {
        groups.put(group.getName(), group);
        saveToDisk(group);
        eventBus.publish(new GroupCreateEvent(group));
        log.info("Group created: {}", group.getName());
    }

    @Override
    public void updateGroup(ServiceGroup group) {
        groups.put(group.getName(), group);
        saveToDisk(group);
        eventBus.publish(new GroupUpdateEvent(group));
        log.info("Group updated: {}", group.getName());
    }

    @Override
    public void deleteGroup(String name) {
        ServiceGroup group = groups.remove(name);
        deleteFromDisk(name);
        if (group != null) eventBus.publish(new GroupDeleteEvent(group));
        log.info("Group deleted: {}", name);
    }

    @Override
    public Optional<ServiceGroup> getGroup(String name) {
        return Optional.ofNullable(groups.get(name));
    }

    @Override
    public Collection<ServiceGroup> getAllGroups() {
        return groups.values();
    }

    // ── persistence ──────────────────────────────────────────────────────────

    private void loadFromDisk() {
        try {
            Files.createDirectories(GROUP_DIR);
            try (var stream = Files.list(GROUP_DIR)) {
                stream.filter(p -> p.toString().endsWith(".json"))
                        .forEach(this::loadFile);
            }
            log.info("Loaded {} group(s) from disk.", groups.size());
        } catch (IOException e) {
            log.error("Failed to load groups from disk: {}", e.getMessage());
        }
    }

    private void loadFile(Path file) {
        try {
            ServiceGroupImpl group = MAPPER.readValue(file.toFile(), ServiceGroupImpl.class);
            groups.put(group.getName(), group);
        } catch (IOException e) {
            log.warn("Failed to load group file '{}': {}", file, e.getMessage());
        }
    }

    private void saveToDisk(ServiceGroup group) {
        try {
            Files.createDirectories(GROUP_DIR);
            Path file = GROUP_DIR.resolve(group.getName() + ".json");
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), group);
        } catch (IOException e) {
            log.error("Failed to save group '{}': {}", group.getName(), e.getMessage());
        }
    }

    private void deleteFromDisk(String