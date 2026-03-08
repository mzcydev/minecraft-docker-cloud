package dev.cloud.master.console.commands;

/**
 * Contract for all console commands executable by the master CLI.
 */
public interface Command {

    /**
     * Executes the command with the given arguments.
     *
     * @param sender the entity executing the command
     * @param args   the arguments passed after the command name
     */
    void execute(CommandSender sender, String[] args);

    /**
     * Returns the primary name of this command (e.g. {@code "group"}).
     */
    String getName();

    /**
     * Returns a short description shown in the help output.
     */
    String getDescription();

    /**
     * Returns the usage string shown when the command is used incorrectly.
     * Example: {@code "group create <name> <type>"}
     */
    String getUsage();
}