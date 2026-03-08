package dev.cloud.networking.player;

import dev.cloud.proto.common.ProtoUUID;
import dev.cloud.proto.player.*;
import dev.cloud.proto.common.Response;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Stub wrapper for calling a proxy plugin's {@link PlayerExecutorServiceGrpc}.
 * The master uses this to perform actions on players (send, kick, message)
 * by delegating to the proxy that owns the connection.
 */
public class PlayerRpcClient {

    private static final Logger log = LoggerFactory.getLogger(PlayerRpcClient.class);

    private final PlayerExecutorServiceGrpc.PlayerExecutorServiceBlockingStub stub;
    private final String proxyName;

    /**
     * @param channel   the open channel to the proxy plugin's gRPC server
     * @param proxyName the name of the proxy (used for logging)
     */
    public PlayerRpcClient(ManagedChannel channel, String proxyName) {
        this.stub = PlayerExecutorServiceGrpc.newBlockingStub(channel);
        this.proxyName = proxyName;
    }

    /**
     * Instructs the proxy to connect the given player to a different service.
     *
     * @param playerId    the player's UUID
     * @param serviceName the target service name
     */
    public Response sendPlayer(UUID playerId, String serviceName) {
        log.debug("Sending player {} to '{}' via proxy '{}'", playerId, serviceName, proxyName);
        return stub.sendPlayer(SendPlayerRequest.newBuilder()
                .setUniqueId(toProto(playerId))
                .setServiceName(serviceName)
                .build());
    }

    /**
     * Instructs the proxy to kick the given player with a reason.
     *
     * @param playerId the player's UUID
     * @param reason   the kick message shown to the player
     */
    public Response kickPlayer(UUID playerId, String reason) {
        log.debug("Kicking player {} via proxy '{}': {}", playerId, proxyName, reason);
        return stub.kickPlayer(KickPlayerRequest.newBuilder()
                .setUniqueId(toProto(playerId))
                .setReason(reason)
                .build());
    }

    /**
     * Instructs the proxy to send a chat message to the given player.
     *
     * @param playerId the player's UUID
     * @param message  the message text (plain text or MiniMessage format)
     */
    public Response messagePlayer(UUID playerId, String message) {
        return stub.messagePlayer(MessagePlayerRequest.newBuilder()
                .setUniqueId(toProto(playerId))
                .setMessage(message)
                .build());
    }

    private ProtoUUID toProto(UUID uuid) {
        return ProtoUUID.newBuilder()
                .setMostSignificantBits(uuid.getMostSignificantBits())
                .setLeastSignificantBits(uuid.getLeastSignificantBits())
                .build();
    }
}