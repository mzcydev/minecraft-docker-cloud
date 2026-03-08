package dev.cloud.api.event;

/**
 * Central event bus for publish/subscribe communication between cloud components.
 * Listeners are registered per event type and called in priority order.
 */
public interface EventBus {

    /**
     * Registers all {@code @EventHandler}-annotated methods in the given listener object.
     *
     * @param listener the object containing handler methods
     */
    void subscribe(Object listener);

    /**
     * Unregisters all handler methods previously registered from the given listener.
     *
     * @param listener the listener to remove
     */
    void unsubscribe(Object listener);

    /**
     * Publishes an event to all registered listeners in priority order.
     * If the event implements {@link Cancellable} and is cancelled,
     * lower-priority listeners still receive it (use {@link EventPriority#MONITOR} to observe).
     *
     * @param event the event to publish
     * @param <T>   the event type
     * @return the event instance after all listeners have processed it
     */
    <T extends CloudEvent> T publish(T event);
}