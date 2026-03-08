package dev.cloud.api.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Validates that all required keys are present in a configuration map.
 */
public class ConfigValidator {

    /**
     * Checks that every required key exists and is non-null in the given config map.
     *
     * @param config       the configuration map to validate
     * @param requiredKeys the keys that must be present
     * @return a list of error messages for any missing keys; empty if all are present
     */
    public List<String> validate(Map<String, Object> config, String... requiredKeys) {
        List<String> errors = new ArrayList<>();
        for (String key : requiredKeys) {
            if (!config.containsKey(key) || config.get(key) == null) {
                errors.add("Missing required config key: '" + key + "'");
            }
        }
        return errors;
    }
}