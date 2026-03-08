package dev.cloud.api.group;

import java.util.Collection;
import java.util.Optional;

/**
 * Manages all registered {@link ServiceGroup} configurations.
 * Implementations persist groups to disk and cache them in memory.
 */
public interface GroupManager {

    /**
     * Registers and persists a new group.
     *
     * @param group the group to create
     * @throws IllegalArgumentException if a group with the same name already exists
     */
    void createGroup(ServiceGroup group);

    /**
     * Updates an existing group's configuration and persists the changes.
     *
     * @param group the group with updated values
     */
    void updateGroup(ServiceGroup group);

    /**
     * Deletes the group with the given name.
     * Running services of this group are not automatically stopped.
     *
     * @param name the name of the group to delete
     */
    void deleteGroup(String name);

    /**
     * Looks up a group by name.
     *
     * @param name the group name (case-insensitive)
     * @return an {@link Optional} containing the group, or empty if not found
     */
    Optional<ServiceGroup> getGroup(String name);

    /**
     * Returns all registered groups.
     */
    Collection<ServiceGroup> getAllGroups();

    /**
     * Returns {@code true} if a group with the given name is registered.
     *
     * @param name the group name to check
     */
    boolean existsGroup(String name);
}