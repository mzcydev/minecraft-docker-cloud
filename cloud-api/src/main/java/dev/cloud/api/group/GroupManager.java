package dev.cloud.api.group;

import java.util.Collection;
import java.util.Optional;

public interface GroupManager {
    void createGroup(ServiceGroup group);
    void updateGroup(ServiceGroup group);
    void deleteGroup(String name);
    Optional<ServiceGroup> getGroup(String name);
    Collection<ServiceGroup> getAllGroups();
    default boolean existsGroup(String name) { return getGroup(name).isPresent(); }
}
