package dev.cloud.api.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an event handler to be registered by the {@link EventBus}.
 * The method must have exactly one parameter which is the event type to listen for.
 *
 * <pre>{@code
 * @EventHandler(priority = EventPriority.HIGH)
 * public void onServiceStart(ServiceStartEvent event) {
 *     // handle event
 * }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventHandler {

    /**
     * The priority at which this handler is called relative to others.
     * Defaults to {@link EventPriority#NORMAL}.
     */
    EventPriority priority() default EventPriority.NORMAL;

    /**
     * If {@code true}, this handler will also be called even if the event
     * has been cancelled by a higher-priority listener.
     */
    boolean ignoreCancelled() default false;
}