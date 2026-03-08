package dev.cloud.api.group;

/**
 * Defines the type of Minecraft platform a service runs.
 */
public enum ServiceType {

    /** Velocity reverse proxy. */
    VELOCITY,

    /** BungeeCord reverse proxy. */
    BUNGEECORD,

    /** Paper or Spigot game server. */
    PAPER,

    /** Fabric mod loader game server. */
    FABRIC
}