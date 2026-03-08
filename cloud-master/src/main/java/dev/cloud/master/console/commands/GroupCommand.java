package dev.cloud.master.console.commands;

import dev.cloud.api.group.ServiceGroup;
import dev.cloud.api.group.ServiceGroupImpl;
import dev.cloud.api.group.ServiceType;
import dev.cloud.master.group.MasterGroupManager;

/**
 * Console command for managing service groups.
 * Usage: group <create|delete|list|info> [args...]
 */
public class GroupCommand implements Command {

    private final MasterGroupManager groupManager;

    public GroupCommand(MasterGroupManager groupManager) {
        this.groupManager = groupManager;
    }

    @Override
    public String getName() {
        return "group";
    }

    @Override
    public String getDescription() {
        return "Manage service groups.";
    }

    @Override
    public String getUsage() {
        return "group <create|delete|list|info> [name] [type]";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Usage: " + getUsage());
            return;
        }

        switch (args[0].toLowerCase()) {
            case "list" -> {
                var groups = groupManager.getAllGroups();
                if (groups.isEmpty()) {
                    sender.sendMessage("No groups configured.");
                    return;
                }
                groups.forEach(g -> sender.sendMessage(
                        "  » " + g.getName() + " [" + g.getType() + "] min=" +
                                g.getMinServices() + " max=" + g.getMaxServices()));
            }
            case "create" -> {
                if (args.length < 3) {
                    sender.sendMessage("Usage: group create <name> <type>");
                    return;
                }
                try {
                    ServiceType type = ServiceType.valueOf(args[2].toUpperCase());
                    ServiceGroup group = new ServiceGroupImpl(
                            args[1], type, args[1].toLowerCase(),
                            512, 100, 1, 5, null, false
                    );
                    groupManager.createGroup(group);
                    sender.sendMessage("Group '" + args[1] + "' created.");
                } catch (IllegalArgumentException e) {
                    sender.sendMessage("Unknown type: " + args[2] +
                            ". Valid: PAPER, VELOCITY, BUNGEECORD, FABRIC");
                }
            }
            case "delete" -> {
                if (args.length < 2) {
                    sender.sendMessage("Usage: group delete <name>");
                    return;
                }
                groupManager.deleteGroup(args[1]);
                sender.sendMessage("Group '" + args[1] + "' deleted.");
            }
            case "info" -> {
                if (args.length < 2) {
                    sender.sendMessage("Usage: group info <name>");
                    return;
                }
                groupManager.getGroup(args[1]).ifPresentOrElse(g -> {
                    sender.sendMessage("Group: " + g.getName());
                    sender.sendMessage("  Type:     " + g.getType());
                    sender.sendMessage("  Template: " + g.getTemplateName());
                    sender.sendMessage("  Memory:   " + g.getMemory() + "MB");
                    sender.sendMessage("  Players:  max " + g.getMaxPlayers());
                    sender.sendMessage("  Services: " + g.getMinServices() + "-" + g.getMaxServices());
                }, () -> sender.sendMessage("Group not found: " + args[1]));
            }
            default -> sender.sendMessage("Usage: " + getUsage());
        }
    }
}