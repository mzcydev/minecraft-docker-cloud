package dev.cloud.master.console.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for all console commands.
 * Commands are registered at startup and looked up by name when the user types input.
 */
public class CommandManager {

    private static final Logger log = LoggerFactory.getLogger(CommandManager.class);

    private final Map<String, Command> commands = new ConcurrentHashMap<>();

    /**
     * Registers a command. The command's name (lowercased) is used as the key.
     *
     * @param command the command to register
     */
    public void register(Command command) {
        commands.put(command.getName().toLowerCase(), command);
        log.debug("Command registered: {}", command.getName());
    }

    /**
     * Dispatches a raw input line to the matching command.
     *
     * @param sender the sender executing the input
     * @param input  the full input line (e.g. {@code "group create Lobby PAPER"})
     */
    public void dispatch(CommandSender sender, String input) {
        if (input == null || input.isBlank()) return;

        String[] parts = input.trim().split("\\s+");
        String name = parts[0].toLowerCase();
        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);

        Optional<Command> command = Optional.ofNullable(commands.get(name));
        if (command.isPresent()) {
            try {
                command.get().execute(sender, args);
            } catch (Exception e) {
                sender.sendMessage("§cError executing command: " + e.getMessage());
                log.error("Error executing command '{}': {}", name, e.getMessage(), e);
            }
        } else {
            sender.sendMessage("Unknown command: '" + name + "'. Type 'help' for a list of commands.");
        }
    }

    /**
     * Returns all registered commands.
     */
    public Collection<Command> allCommands() {
        return commands.values();
    }

    /**
     * Finds a command by name.
     */
    public Optional<Command> find(String name) {
        return Optional.ofNullable(commands.get(name.toLowerCase()));
    }
}