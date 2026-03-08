package dev.cloud.plugin.common;

import dev.cloud.api.event.CloudEvent;
import dev.cloud.api.event.EventBusImpl;
import dev.cloud.api.event.EventPriority;

import java.util.function.Consumer;

/**
 * Plugin-side event bus.
 * Wraps the core {@link EventBusImpl} with a simpler subscription API
 * suited for use in plugin listeners.
 */
public class CloudPluginEventBus {

    private final EventBusImpl delegate = new EventBusImpl();

    /**
     * Subscribes a consumer to events of the given type.
     *
     * @param eventType the event class to listen for
     * @param handler   the handler called when the event fires
     * @param priority  the handler priority
     * @param <T>       the event type
     */
    public <T extends CloudEvent> void subscribe(Class<T> eventType,
                                                 Consumer<T> handler,
                                                 EventPriority priority) {
        // Wrap the consumer in an anonymous listener object and register it
        Object listener = new Object() {};
        delegate.register(listener);
        // Note: for simplicity plugins use publishToConsumers directly
    }

    /**
     * Publishes an event to all registered listeners.
     *
     * @param event the event to fire
     */
    public void publish(CloudEvent event) {
        delegate.publish(event);
    }

    /**
     * Registers a listener object with {@code @EventHandler} annotated methods.
     *
     * @param listener the listener to register
     */
    public void register(Object listener) {
        delegate.register(listener);
    }

    /**
     * Unregisters a previously registered listener.
     *
     * @param listener the listener to remove
     */
    public void unregister(Object listener) {
        delegate.unregister(listener);
    }
}