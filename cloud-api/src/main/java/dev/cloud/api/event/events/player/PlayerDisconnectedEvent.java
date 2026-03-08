package dev.cloud.api.event.events.player;

import dev.cloud.api.event.CloudEvent;
import dev.cloud.api.player.CloudPlayer;

/**
 * Fired after a player disconnects from the network.
 */
public class PlayerDisconnectedEvent extends CloudEvent {
    private final CloudPlayer player;

    public PlayerDisconnectedEvent(CloudPlayer player) {
        this.player = player;
    }

    public CloudPlayer getPlayer() {
        return player;
    }
}