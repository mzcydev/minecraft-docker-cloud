package dev.cloud.api.event.events.player;

import dev.cloud.api.event.Cancellable;
import dev.cloud.api.event.CloudEvent;

import java.util.UUID;

/**
 * Fired when a player attempts to connect to the network. Can be cancelled to deny entry.
 */
public class PlayerLoginEvent extends CloudEvent implements Cancellable {
    private final UUID uniqueId;
    private final String name;
    private final String address;
    private boolean cancelled;
    private String cancelReason = "You are not allowed to join.";

    public PlayerLoginEvent(UUID uniqueId, String name, String address) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.address = address;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String reason) {
        this.cancelReason = reason;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean c) {
        this.cancelled = c;
    }
}