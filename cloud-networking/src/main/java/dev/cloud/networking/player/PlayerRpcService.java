package dev.cloud.networking.player;

import dev.cloud.api.event.EventBus;
import dev.cloud.api.event.events.player.PlayerConnectedEvent;
import dev.cloud.api.event.events.player.PlayerDisconnectedEvent;
import dev.cloud.api.event.events.player.PlayerSwitchServiceEvent;
import dev.cloud.api.player.CloudPlayerImpl;
import dev.cloud.api.player.PlayerManager;
import dev.cloud.proto.common.Empty;
import dev.cloud.proto.player.*;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Master-side gRPC implementation of {@link PlayerReporterGrpc.PlayerReporterImplBase}.
 * Receives player lifecycle events from proxy plugins and updates the player registry.
 */
public class PlayerRpcService extends PlayerReporterGrpc.PlayerReporterImplBase {

    private static final Logger log = LoggerFactory.getLogger(PlayerRpcService.class);

    private final PlayerManager playerManager;
    private final EventBus eventBus;

    public PlayerRpcService(PlayerManager playerManager, EventBus eventBus) {
        this.playerManager = playerManager;
        this.eventBus = eventBus;
    }

    /**
     * Called by a proxy plugin when a player successfully joins the network.
     */
    @Override
    public void reportConnect(PlayerConnectRequest request, StreamObserver<Empty> observer) {
        ProtoPlayer p = request.getPlayer();
        UUID uuid = toUUID(p.getUniqueId());

        CloudPlayerImpl player = new CloudPlayerImpl(
                uuid,
                p.getName(),
                p.getCurrentService(),
                p.getProxyName(),
                p.getAddress()
        );

        playerManager.registerPlayer(player);
        eventBus.publish(new PlayerConnectedEvent(player));
        log.debug("Player '{}' connected via proxy '{}'", p.getName(), p.getProxyName());

        observer.onNext(Empty.getDefaultInstance());
        observer.onCompleted();
    }

    /**
     * Called by a proxy plugin when a player disconnects from the network.
     */
    @Override
    public void reportDisconnect(PlayerDisconnectRequest request, StreamObserver<Empty> observer) {
        UUID uuid = toUUID(request.getUniqueId());

        playerManager.getPlayer(uuid).ifPresent(player -> {
            eventBus.publish(new PlayerDisconnectedEvent(player));
            log.debug("Player '{}' disconnected.", player.getName());
        });

        playerManager.unregisterPlayer(uuid);

        observer.onNext(Empty.getDefaultInstance());
        observer.onCompleted();
    }

    /**
     * Called by a proxy plugin when a player switches from one backend service to another.
     */
    @Override
    public void reportSwitch(PlayerSwitchRequest request, StreamObserver<Empty> observer) {
        UUID uuid = toUUID(request.getUniqueId());

        playerManager.getPlayer(uuid).ifPresent(player -> {
            if (player instanceof CloudPlayerImpl impl) {
                impl.setCurrentService(request.getNewService());
            }
            eventBus.publish(new PlayerSwitchServiceEvent(
                    player,
                    request.getPreviousService(),
                    request.getNewService()
            ));
            log.debug("Player '{}' switched: {} → {}", player.getName(),
                    request.getPreviousService(), request.getNewService());
        });

        observer.onNext(Empty.getDefaultInstance());
        observer.onCompleted();
    }

    private UUID toUUID(dev.cloud.proto.common.ProtoUUID proto) {
        return new UUID(proto.getMostSignificantBits(), proto.getLeastSignificantBits());
    }
}