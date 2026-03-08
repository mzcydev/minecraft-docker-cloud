package dev.cloud.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Holds all configuration values for the master process.
 * Loaded once at startup from {@code master.yml}.
 */
public record MasterConfig(
        String host,
        int grpcPort,
        int restPort,
        String authToken,
        String templateDir,
        int maxNodes,
        int serviceStartTimeoutSeconds
) {
    private static final Logger log = LoggerFactory.getLogger(MasterConfig.class);
    private static final Path CONFIG_PATH = Path.of("master.yml");

    public static MasterConfig load() throws IOException {
        if (!Files.exists(CONFIG_PATH)) {
            writeDefaults();
            log.info("Created default master.yml — please edit and restart.");
            System.exit(0);
        }

        Yaml yaml = new Yaml();
        try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
            Map<String, Object> root = yaml.load(in);
            Map<String, Object> service = section(root, "service");

            return new MasterConfig(
                    str(root, "host"),
                    num(root, "grpcPort"),
                    num(root, "restPort"),
                    str(root, "authToken"),
                    str(root, "templateDir"),
                    num(root, "maxNodes"),
                    num(service, "startTimeoutSeconds")
            );
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> section(Map<String, Object> map, String key) {
        return (Map<String, Object>) map.get(key);
    }

    private static String str(Map<String, Object> map, String key) {
        return String.valueOf(map.get(key));
    }

    private static int num(Map<String, Object> map, String key) {
        return ((Number) map.get(key)).intValue();
    }

    private static void writeDefaults() throws IOException {
        String defaults = """
                host: 0.0.0.0
                grpcPort: 9090
                restPort: 8080
                authToken: change-me
                templateDir: templates/
                maxNodes: 10
                service:
                  startTimeoutSeconds: 30
                """;
        Files.writeString(CONFIG_PATH, defaults);
    }
}