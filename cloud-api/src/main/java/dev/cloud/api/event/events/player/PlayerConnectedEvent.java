package dev.cloud.api.event.events.player;
import dev.cloud.api.event.CloudEvent;
import dev.cloud.api.player.CloudPlayer;

/** Fired after a player has fully connected to the network and been registered. */
public class PlayerConnectedEvent extends CloudEvent {
    private final CloudPlayer player;
    public PlayerConnectedEvent(CloudPlayer player) { this.player = player; }
    public CloudPlayer getPlayer() { return player; }
}