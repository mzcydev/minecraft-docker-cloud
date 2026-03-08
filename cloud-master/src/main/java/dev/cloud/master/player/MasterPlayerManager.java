package dev.cloud.master.player;

import dev.cloud.api.event.EventBus;
import dev.cloud.api.event.events.player.PlayerConnectedEvent;
import dev.cloud.api.event.events.player.PlayerDisconnectedEvent;
import dev.cloud.api.event.events.player.PlayerSwitchServiceEvent;
import dev.cloud.api.player.CloudPlayer;
import dev.cloud.api.player.CloudPlayerImpl;
import dev.cloud.api.player.PlayerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks all players currently connected to the cloud network.
 * Updated by the gRPC {@code PlayerRpcService} when the proxy reports connect/disconnect/switch events.
 */
public class MasterPlayerManager implements PlayerManager {

    private static final Logger log = LoggerFactory.getLogger(MasterPlayerManager.class);

    private final Map<UUID, CloudPlayerImpl> byUuid = new ConcurrentHashMap<>();
    private final Map<String, CloudPlayerImpl> byName = new ConcurrentHashMap<>();

    private final EventBus eventBus;

    public MasterPlayerManager(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * Registers a player that has just connected to the network.
     *
     * @param player the connecting player
     */
    public void registerConnect(CloudPlayerImpl player) {
        byUuid.put(player.getUuid(), player);
        byName.put(player.getName().toLowerCase(), player);
        eventBus.publish(new PlayerConnectedEvent(player));
        log.debug("Player connected: {} ({})", player.getName(), player.getUuid());
    }

    /**
     * Removes a player that has disconnected from the network.
     *
     * @param uuid the UUID of the disconnecting player
     */
    public void registerDisconnect(UUID uuid) {
        CloudPlayerImpl player = byUuid.remove(uuid);
        if (player != null) {
            byName.remove(player.getName().toLowerCase());
            eventBus.publish(new PlayerDisconnectedEvent(player));
            log.debug("Player disconnected: {}", player.getName());
        }
    }

    /**
     * Updates the current service of a player that has switched servers.
     *
     * @param uuid           the UUID of the switching player
     * @param newServiceName the name of the new service
     */
    public void registerSwitch(UUID uuid, String newServiceName) {
        CloudPlayerImpl player = byUuid.get(uuid);
        if (player != null) {
            String oldService = player.getCurrentService();
            player.setCurrentService(newServiceName);
            eventBus.publish(new PlayerSwitchServiceEvent(player, oldService, newServiceName));
            log.debug("Player '{}' switched: {} → {}", player.getName(), oldService, newServiceName);
        }
    }

    @Override
    public Optional<CloudPlayer> getPlayer(UUID uuid) {
        return Optional.ofNullable(byUuid.get(uuid));
    }

    @Override
    public Optional<CloudPlayer> getPlayer(String name) {
        return Optional.ofNullable(byName.get(name.toLowerCase()));
    }

    @Override
    public Collection<CloudPlayerImpl> allPlayers() {
        return byUuid.values();
    }

    @Override
    public int onlineCount() {
        return byUuid.size();
    }
}