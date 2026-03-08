package dev.cloud.api.event.events.player;
import dev.cloud.api.event.CloudEvent;
import dev.cloud.api.player.CloudPlayer;

/** Fired when a player switches from one service to another within the network. */
public class PlayerSwitchServiceEvent extends CloudEvent {
    private final CloudPlayer player;
    private final String previousService;
    private final String newService;

    public PlayerSwitchServiceEvent(CloudPlayer player, String previousService, String newService) {
        this.player = player;
        this.previousService = previousService;
        this.newService = newService;
    }

    public CloudPlayer getPlayer()       { return player; }
    public String getPreviousService()   { return previousService; }
    public String getNewService()        { return newService; }
}