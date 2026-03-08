package dev.cloud.api.service;

/**
 * Defines how a service is managed over its lifetime.
 */
public enum ServiceLifecycle {

    /**
     * Automatically managed by the scheduler based on group min/max settings.
     */
    DYNAMIC,

    /**
     * Runs permanently; world data and files are preserved across restarts.
     */
    STATIC,

    /**
     * Started manually; not subject to automatic scheduling or shutdown.
     */
    MANUAL
}