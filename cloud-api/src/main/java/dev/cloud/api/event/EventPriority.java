package dev.cloud.api.event;

/**
 * Defines the order in which event listeners are called.
 * Lower ordinal = called earlier. {@link #MONITOR} is always last
 * and should only be used for observing the final outcome.
 */
public enum EventPriority {
    LOWEST,
    LOW,
    NORMAL,
    HIGH,
    HIGHEST,
    MONITOR
}