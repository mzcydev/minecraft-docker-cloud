package dev.cloud.rest;

import org.yaml.snakeyaml.Yaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Configuration for the REST API server.
 * Loaded from {@code master.yml} under the {@code rest:} section.
 */
public record RestConfig(
        int port,
        String authToken,
        boolean corsEnabled,
        String corsOrigin
) {
    private static final Logger log = LoggerFactory.getLogger(RestConfig.class);

    public static RestConfig load() throws IOException {
        Path path = Path.of("master.yml");
        Yaml yaml = new Yaml();
        try (InputStream in = Files.newInputStream(path)) {
            Map<String, Object> root = yaml.load(in);
            @SuppressWarnings("unchecked")
            Map<String, Object> rest = (Map<String, Object>) root.getOrDefault("rest", Map.of());

            return new RestConfig(
                    ((Number) rest.getOrDefault("port", 8080)).intValue(),
                    String.valueOf(root.getOrDefault("authToken", "change-me")),
                    (boolean) rest.getOrDefault("corsEnabled", true),
                    String.valueOf(rest.getOrDefault("corsOrigin", "*"))
            );
        }
    }
}