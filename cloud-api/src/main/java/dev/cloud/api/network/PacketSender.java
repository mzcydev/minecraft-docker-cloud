package dev.cloud.api.network;

/**
 * Sends structured messages to one or more remote components over gRPC.
 */
public interface PacketSender {

    /**
     * Sends a message to the component identified by the given name.
     *
     * @param targetName the name of the target component (e.g. a node name)
     * @param message    the serialized message payload
     */
    void send(String targetName, byte[] message);

    /**
     * Broadcasts a message to all currently connected components of the given type.
     *
     * @param type    the component type to broadcast to
     * @param message the serialized message payload
     */
    void broadcast(NetworkComponent type, byte[] message);
}