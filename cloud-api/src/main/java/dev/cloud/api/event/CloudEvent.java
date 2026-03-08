package dev.cloud.api.event;

import java.time.Instant;

/**
 * Base class for all events published on the {@link EventBus}.
 * Carries a timestamp of when the event was created.
 */
public abstract class CloudEvent {

    private final Instant timestamp;

    protected CloudEvent() {
        this.timestamp = Instant.now();
    }

    /**
     * Returns the moment this event was created.
     */
    public Instant getTimestamp() {
        return timestamp;
    }
}