package dev.cloud.api.event;

/**
 * Marks an event as cancellable. If cancelled, the action that triggered
 * the event should not be executed.
 */
public interface Cancellable {

    /**
     * Returns {@code true} if this event has been cancelled.
     */
    boolean isCancelled();

    /**
     * Cancels or un-cancels this event.
     *
     * @param cancelled {@code true} to cancel, {@code false} to un-cancel
     */
    void setCancelled(boolean cancelled);
}