package dev.cloud.api.group;

/**
 * Defines the configuration template for a group of services.
 */
public interface ServiceGroup {

    String getName();
    ServiceType getType();
    ServiceEnvironment getEnvironment();
    String getTemplateName();
    int getMinServices();
    int getMaxServices();
    int getMaxPlayers();
    int getMemory();
    boolean isStatic();
    boolean isMaintenance();
    String getJvmFlags();
    int getStartPort();
}
