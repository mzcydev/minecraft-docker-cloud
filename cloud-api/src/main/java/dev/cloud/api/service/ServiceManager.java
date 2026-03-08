package dev.cloud.api.service;

import java.util.Collection;
import java.util.Optional;

public interface ServiceManager {
    Optional<CloudService> getService(String name);
    Collection<CloudService> getAllServices();
    Collection<CloudService> getServicesByGroup(String groupName);
    void updateState(String serviceName, ServiceState state);
    void updatePlayerCount(String serviceName, int count);
}
