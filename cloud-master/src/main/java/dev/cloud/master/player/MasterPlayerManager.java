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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MasterPlayerManager implements PlayerManager {

    private static final Logger log = LoggerFactory.getLogger(MasterPlayerManager.class);

    private final Map<UUID, CloudPlayerImpl> byUuid = new ConcurrentHashMap<>();
    private final Map<String, CloudPlayerImpl> byName = new ConcurrentHashMap<>();
    private final EventBus eventBus;

    public MasterPlayerManager(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void registerPlayer(CloudPlayer player) {
        CloudPlayerImpl impl = (CloudPlayerImpl) player;
        byUuid.put(impl.getUuid(), impl);
        byName.put(impl.getName().toLowerCase(), impl);
        eventBus.publish(new PlayerConnectedEvent(impl));
        log.debug("Player connected: {} ({})", impl.getName(), impl.getUuid());
    }

    @Override
    public void unregisterPlayer(UUID uniqueId) {
        CloudPlayerImpl player = byUuid.remove(uniqueId);
        if (player != null) {
            byName.remove(player.getName().toLowerCase());
            eventBus.publish(new PlayerDisconnectedEvent(player));
            log.debug("Player disconnected: {}", player.getName());
        }
    }

    @Override
    public Optional<CloudPlayer> getPlayer(UUID uniqueId) {
        return Optional.ofNullable(byUuid.get(uniqueId));
    }

    @Override
    public Optional<CloudPlayer> getPlayer(String name) {
        return Optional.ofNullable(byName.get(name.toLowerCase()));
    }

    @Override
    public Collection<CloudPlayer> getAllPlayers() {
        return List.copyOf(byUuid.values());
    }

    @Override
    public int getOnlineCount() {
        return byUuid.size();
    }

    /** Update a player's current service on server-switch. */
    public void registerSwitch(UUID uniqueId, String newServiceName) {
        CloudPlayerImpl player = byUuid.get(uniqueId);
        if (player != null) {
            String oldService = player.getCurrentService();
            player.setCurrentService(newServiceName);
            eventBus.publish(new PlayerSwitchServiceEvent(player, oldService, newServiceName));
            log.debug("Player '{}' switched: {} → {}", player.getName(), oldService, newServiceName);
        }
    }

    public Collection<CloudPlayerImpl> allPlayerImpls() {
        return byUuid.values();
    }
}
