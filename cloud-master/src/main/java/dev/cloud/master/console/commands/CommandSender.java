package dev.cloud.master.console.commands;

/**
 * Represents an entity that can send commands and receive output.
 * Currently only the console is a sender, but this abstraction
 * allows future REST/plugin senders.
 */
public interface CommandSender {

    /**
     * Sends a message back to this sender.
     *
     * @param message the message to display
     */
    void sendMessage(String message);

    /**
     * Returns the name of this sender (e.g. {@code "CONSOLE"}).
     */
    String getName();
}