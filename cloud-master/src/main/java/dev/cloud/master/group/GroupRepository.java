package dev.cloud.master.group;

import dev.cloud.api.group.ServiceGroup;
import dev.cloud.api.group.ServiceGroupImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles reading and writing of service group configurations to disk as JSON files.
 * Each group is stored in its own {@code groups/<name>.json} file.
 */
public class GroupRepository {

    private static final Logger log = LoggerFactory.getLogger(GroupRepository.class);
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private final Path groupDir;

    public GroupRepository(Path groupDir) {
        this.groupDir = groupDir;
        try { Files.createDirectories(groupDir); }
        catch (IOException e) { throw new RuntimeException("Cannot create group dir", e); }
    }

    /**
     * Loads all groups from disk.
     */
    public List<ServiceGroup> loadAll() {
        List<ServiceGroup> result = new ArrayList<>();
        try (var stream = Files.list(groupDir)) {
            stream.filter(p -> p.toString().endsWith(".json"))
                    .forEach(file -> {
                        try {
                            result.add(MAPPER.readValue(file.toFile(), ServiceGroupImpl.class));
                        } catch (IOException e) {
                            log.warn("Failed to load group '{}': {}", file, e.getMessage());
                        }
                    });
        } catch (IOException e) {
            log.error("Failed to list group directory: {}", e.getMessage());
        }
        return result;
    }

    /**
     * Saves a group to disk.
     */
    public void save(ServiceGroup group) {
        try {
            Path file = groupDir.resolve(group.getName() + ".json");
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), group);
        } catch (IOException e) {
            log.error("Failed to save group '{}': {}", group.getName(), e.getMessage());
        }
    }

    /**
     * Deletes a group file from disk.
     */
    public void delete(String name) {
        try {
            Files.deleteIfExists(groupDir.resolve(name + ".json"));
        } catch (IOException e) {
            log.warn("Failed to delete group '{}': {}", name, e.getMessage());
        }
    }
}