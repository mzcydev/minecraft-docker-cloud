package dev.cloud.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Holds all configuration values for the node daemon.
 * Loaded once at startup from {@code node.yml}.
 */
public record NodeConfig(
        String nodeName,
        String host,
        int grpcPort,
        String masterHost,
        int masterPort,
        String authToken,
        String dockerHost,
        int portRangeFrom,
        int portRangeTo,
        int maxMemoryMb,
        int maxServices,
        String templateCacheDir,
        String serviceWorkDir
) {
    private static final Logger log = LoggerFactory.getLogger(NodeConfig.class);
    private static final Path CONFIG_PATH = Path.of("node.yml");

    /**
     * Loads the node configuration from {@code node.yml} in the working directory.
     * Creates a default config file if none exists.
     */
    public static NodeConfig load() throws IOException {
        if (!Files.exists(CONFIG_PATH)) {
            writeDefaults();
            log.info("Created default node.yml — please edit it and restart.");
            System.exit(0);
        }

        Yaml yaml = new Yaml();
        try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
            Map<String, Object> root = yaml.load(in);
            Map<String, Object> master   = section(root, "master");
            Map<String, Object> docker   = section(root, "docker");
            Map<String, Object> ports    = section(root, "ports");
            Map<String, Object> resource = section(root, "resources");
            Map<String, Object> dirs     = section(root, "directories");

            return new NodeConfig(
                    str(root, "name"),
                    str(root, "host"),
                    num(root, "grpcPort"),
                    str(master, "host"),
                    num(master, "port"),
                    str(master, "authToken"),
                    str(docker, "host"),
                    num(ports, "from"),
                    num(ports, "to"),
                    num(resource, "maxMemoryMb"),
                    num(resource, "maxServices"),
                    str(dirs, "templateCache"),
                    str(dirs, "serviceWork")
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
                name: node-1
                host: 127.0.0.1
                grpcPort: 9091
                master:
                  host: 127.0.0.1
                  port: 9090
                  authToken: change-me
                docker:
                  host: unix:///var/run/docker.sock
                ports:
                  from: 30000
                  to: 31000
                resources:
                  maxMemoryMb: 8192
                  maxServices: 20
                directories:
                  templateCache: templates/
                  serviceWork: services/
                """;
        Files.writeString(CONFIG_PATH, defaults);
    }
}