package dev.cloud.master.console.commands;

import dev.cloud.master.node.NodeRegistry;

/**
 * Console command for viewing node status.
 * Usage: node <list|info> [name]
 */
public class NodeCommand implements Command {

    private final NodeRegistry nodeRegistry;

    public NodeCommand(NodeRegistry nodeRegistry) {
        this.nodeRegistry = nodeRegistry;
    }

    @Override public String getName()        { return "node"; }
    @Override public String getDescription() { return "View connected nodes."; }
    @Override public String getUsage()       { return "node <list|info> [name]"; }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) { sender.sendMessage("Usage: " + getUsage()); return; }

        switch (args[0].toLowerCase()) {
            case "list" -> {
                var nodes = nodeRegistry.allNodes();
                if (nodes.isEmpty()) { sender.sendMessage("No nodes connected."); return; }
                nodes.forEach(n -> sender.sendMessage(
                        "  » " + n.getName() + " [" + n.getState() + "] " +
                                n.getUsedMemoryMb() + "/" + n.getMaxMemoryMb() + "MB " +
                                n.getRunningServices() + " services"));
            }
            case "info" -> {
                if (args.length < 2) { sender.sendMessage("Usage: node info <name>"); return; }
                nodeRegistry.findNode(args[1]).ifPresentOrElse(n -> {
                    sender.sendMessage("Node: " + n.getName());
                    sender.sendMessage("  Host:     " + n.getHost());
                    sender.sendMessage("  State:    " + n.getState());
                    sender.sendMessage("  Memory:   " + n.getUsedMemoryMb() + "/" + n.getMaxMemoryMb() + "MB");
                    sender.sendMessage("  Services: " + n.getRunningServices() + "/" + n.getMaxServices());
                }, () -> sender.sendMessage("Node not found: " + args[1]));
            }
            default -> sender.sendMessage("Usage: " + getUsage());
        }
    }
}