package dev.cloud.api.event;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Reflection-based implementation of {@link EventBus}.
 * Scans registered objects for {@link EventHandler}-annotated methods
 * and invokes them in {@link EventPriority} order when an event is published.
 */
public class EventBusImpl implements EventBus {

    /**
     * Maps event types to an ordered list of registered handler entries.
     */
    private final Map<Class<?>, List<HandlerEntry>> handlers = new ConcurrentHashMap<>();

    @Override
    public void subscribe(Object listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(EventHandler.class)) continue;
            if (method.getParameterCount() != 1) continue;

            Class<?> eventType = method.getParameterTypes()[0];
            if (!CloudEvent.class.isAssignableFrom(eventType)) continue;

            EventHandler annotation = method.getAnnotation(EventHandler.class);
            method.setAccessible(true);

            handlers
                    .computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                    .add(new HandlerEntry(listener, method, annotation.priority(), annotation.ignoreCancelled()));

            handlers.get(eventType).sort(Comparator.comparingInt(e -> e.priority().ordinal()));
        }
    }

    @Override
    public void unsubscribe(Object listener) {
        handlers.values().forEach(list -> list.removeIf(e -> e.listener() == listener));
    }

    @Override
    public <T extends CloudEvent> T publish(T event) {
        List<HandlerEntry> entries = handlers.getOrDefault(event.getClass(), List.of());
        for (HandlerEntry entry : entries) {
            if (event instanceof Cancellable c && c.isCancelled() && entry.ignoreCancelled()) {
                continue;
            }
            try {
                entry.method().invoke(entry.listener(), event);
            } catch (Exception ex) {
                throw new RuntimeException("Error in event handler " + entry.method().getName(), ex);
            }
        }
        return event;
    }

    /**
     * Internal record holding all metadata for a single registered handler method.
     */
    private record HandlerEntry(
            Object listener,
            Method method,
            EventPriority priority,
            boolean ignoreCancelled
    ) {
    }
}