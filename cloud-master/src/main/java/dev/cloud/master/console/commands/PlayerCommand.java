package dev.cloud.master.console.commands;

import dev.cloud.master.player.GlobalPlayerRegistry;

/**
 * Console command for viewing online players.
 * Usage: player <list|info> [name]
 */
public class PlayerCommand implements Command {

    private final GlobalPlayerRegistry playerRegistry;

    public PlayerCommand(GlobalPlayerRegistry playerRegistry) {
        this.playerRegistry = playerRegistry;
    }

    @Override
    public String getName() {
        return "player";
    }

    @Override
    public String getDescription() {
        return "View online players.";
    }

    @Override
    public String getUsage() {
        return "player <list|info> [name]";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Usage: " + getUsage());
            return;
        }

        switch (args[0].toLowerCase()) {
            case "list" -> {
                var players = playerRegistry.allPlayers();
                sender.sendMessage("Online players (" + players.size() + "):");
                players.forEach(p -> sender.sendMessage(
                        "  » " + p.getName() + " @ " + p.getCurrentService()));
            }
            case "info" -> {
                if (args.length < 2) {
                    sender.sendMessage("Usage: player info <name>");
                    return;
                }
                playerRegistry.findByName(args[1]).ifPresentOrElse(p -> {
                    sender.sendMessage("Player: " + p.getName());
                    sender.sendMessage("  UUID:    " + p.getUuid());
                    sender.sendMessage("  Service: " + p.getCurrentService());
                }, () -> sender.sendMessage("Player not found: " + args[1]));
            }
            default -> sender.sendMessage("Usage: " + getUsage());
        }
    }
}