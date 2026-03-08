package dev.cloud.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Utility for reading and writing YAML configuration files,
 * with support for deserializing them into typed Java objects via Jackson.
 */
public class ConfigLoader {

    private final ObjectMapper mapper;
    private final Yaml yaml;

    public ConfigLoader() {
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
        this.yaml = new Yaml();
    }

    /**
     * Loads a YAML file and deserializes it into an instance of the given type.
     *
     * @param path   the path to the YAML file
     * @param type   the target class to deserialize into
     * @param <T>    the target type
     * @return the deserialized object
     * @throws IOException if the file cannot be read or parsed
     */
    public <T> T load(Path path, Class<T> type) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            Map<String, Object> raw = yaml.load(in);
            return mapper.convertValue(raw, type);
        }
    }

    /**
     * Serializes an object and writes it to a YAML file, creating parent directories if needed.
     *
     * @param path   the target file path
     * @param object the object to serialize
     * @throws IOException if the file cannot be written
     */
    public void save(Path path, Object object) throws IOException {
        Files.createDirectories(path.getParent());
        Map<String, Object> data = mapper.convertValue(object, mapper.getTypeFactory()
                .constructMapType(Map.class, String.class, Object.class));
        try (OutputStream out = Files.newOutputStream(path)) {
            out.write(yaml.dump(data).getBytes());
        }
    }
}