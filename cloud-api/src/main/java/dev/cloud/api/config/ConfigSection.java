package dev.cloud.api.config;

import java.util.Map;
import java.util.Optional;

/**
 * Provides typed access to a flat or nested map of configuration values.
 */
public class ConfigSection {

    private final Map<String, Object> data;

    public ConfigSection(Map<String, Object> data) {
        this.data = data;
    }

    /**
     * Returns the raw value for the given key, or empty if not present.
     *
     * @param key the configuration key
     */
    public Optional<Object> get(String key) {
        return Optional.ofNullable(data.get(key));
    }

    /**
     * Returns the string value for the given key, or the default if absent.
     *
     * @param key          the configuration key
     * @param defaultValue the fallback value
     */
    public String getString(String key, String defaultValue) {
        return get(key).map(Object::toString).orElse(defaultValue);
    }

    /**
     * Returns the integer value for the given key, or the default if absent or not a number.
     *
     * @param key          the configuration key
     * @param defaultValue the fallback value
     */
    public int getInt(String key, int defaultValue) {
        return get(key).map(v -> {
            try {
                return Integer.parseInt(v.toString());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }).orElse(defaultValue);
    }

    /**
     * Returns the boolean value for the given key, or the default if absent.
     *
     * @param key          the configuration key
     * @param defaultValue the fallback value
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return get(key).map(v -> Boolean.parseBoolean(v.toString())).orElse(defaultValue);
    }
}