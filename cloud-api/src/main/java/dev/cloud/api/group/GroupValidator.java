package dev.cloud.api.group;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates a {@link ServiceGroup} configuration before it is persisted.
 * Collects all constraint violations instead of failing on the first error.
 */
public class GroupValidator {

    /**
     * Validates the given group and returns a list of human-readable error messages.
     * An empty list means the group is valid.
     *
     * @param group the group to validate
     * @return list of validation errors, empty if the group is valid
     */
    public List<String> validate(ServiceGroup group) {
        List<String> errors = new ArrayList<>();

        if (group.getName() == null || group.getName().isBlank())
            errors.add("Group name must not be blank.");

        if (group.getName() != null && !group.getName().matches("[a-zA-Z0-9_\\-]+"))
            errors.add("Group name may only contain letters, digits, underscores and dashes.");

        if (group.getMemory() < 256)
            errors.add("Memory must be at least 256 MB.");

        if (group.getMinOnlineCount() < 0)
            errors.add("Minimum online count must not be negative.");

        if (group.getMaxOnlineCount() < group.getMinOnlineCount())
            errors.add("Maximum online count must be >= minimum online count.");

        if (group.getMaxPlayers() < 1)
            errors.add("Max players must be at least 1.");

        if (group.getStartPort() < 1024 || group.getStartPort() > 65535)
            errors.add("Start port must be between 1024 and 65535.");

        if (group.getTemplateName() == null || group.getTemplateName().isBlank())
            errors.add("Template name must not be blank.");

        return errors;
    }

    /**
     * Returns {@code true} if the group passes all validation rules.
     *
     * @param group the group to check
     */
    public boolean isValid(ServiceGroup group) {
        return validate(group).isEmpty();
    }
}