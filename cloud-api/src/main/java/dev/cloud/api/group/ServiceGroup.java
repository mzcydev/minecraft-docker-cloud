package dev.cloud.api.group;

import dev.cloud.api.service.ServiceLifecycle;

/**
 * Defines the configuration template for a group of services.
 * A group acts as a blueprint — every service started from it inherits these settings.
 */
public interface ServiceGroup {

    /** Returns the unique name of this group (e.g. {@code "Lobby"}, {@code "BedWars"}). */
    String getName();

    /** Returns the platform type this group runs on. */
    ServiceType getServiceType();

    /** Returns the resolved Docker environment for this group's service type. */
    ServiceEnvironment getEnvironment();

    /** Returns the name of the template to copy files from when starting a service. */
    String getTemplateName();

    /** Returns the minimum number of services that should always be online. */
    int getMinOnlineCount();

    /** Returns the maximum number of services allowed to run simultaneously. */
    int getMaxOnlineCount();

    /** Returns the maximum number of players per service instance. */
    int getMaxPlayers();

    /** Returns the RAM limit in megabytes for each Docker container of this group. */
    int getMemory();

    /** Returns the lifecycle type that governs how services of this group are managed. */
    ServiceLifecycle getLifecycle();

    /** Returns {@code true} if the group is in maintenance mode (only admins can join). */
    boolean isMaintenance();

    /** Returns the JVM flags passed to each service at startup (e.g. {@code "-XX:+UseG1GC"}). */
    String getJvmFlags();

    /** Returns the lowest port number from which ports are allocated for this group. */
    int getStartPort();
}