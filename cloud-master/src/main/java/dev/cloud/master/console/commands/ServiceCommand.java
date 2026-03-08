package dev.cloud.master.console.commands;

import dev.cloud.master.service.MasterServiceManager;
import dev.cloud.master.service.ServiceRegistry;

/**
 * Console command for managing running services.
 * Usage: service <list|stop|info> [name]
 */
public class ServiceCommand implements Command {

    private final ServiceRegistry serviceRegistry;
    private final MasterServiceManager serviceManager;

    public ServiceCommand(ServiceRegistry serviceRegistry, MasterServiceManager serviceManager) {
        this.serviceRegistry = serviceRegistry;
        this.serviceManager = serviceManager;
    }

    @Override
    public String getName() {
        return "service";
    }

    @Override
    public String getDescription() {
        return "Manage running services.";
    }

    @Override
    public String getUsage() {
        return "service <list|stop|info> [name]";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Usage: " + getUsage());
            return;
        }

        switch (args[0].toLowerCase()) {
            case "list" -> {
                var services = serviceRegistry.allServices();
                if (services.isEmpty()) {
                    sender.sendMessage("No services running.");
                    return;
                }
                services.forEach(s -> sender.sendMessage(
                        "  » " + s.getName() + " [" + s.getState() + "] " +
                                s.getOnlinePlayers() + "/" + s.getMaxPlayers() + " " +
                                "@ " + s.getNodeName()));
            }
            case "stop" -> {
                if (args.length < 2) {
                    sender.sendMessage("Usage: service stop <name>");
                    return;
                }
                serviceRegistry.findByName(args[1]).ifPresentOrElse(s -> {
                    serviceManager.stopService(s.getId().toString());
                    sender.sendMessage("Stop command sent for '" + args[1] + "'.");
                }, () -> sender.sendMessage("Service not found: " + args[1]));
            }
            case "info" -> {
                if (args.length < 2) {
                    sender.sendMessage("Usage: service info <name>");
                    return;
                }
                serviceRegistry.findByName(args[1]).ifPresentOrElse(s -> {
                    sender.sendMessage("Service: " + s.getName());
                    sender.sendMessage("  Group:   " + s.getGroupName());
                    sender.sendMessage("  Node:    " + s.getNodeName());
                    sender.sendMessage("  State:   " + s.getState());
                    sender.sendMessage("  Players: " + s.getOnlinePlayers() + "/" + s.getMaxPlayers());
                    sender.sendMessage("  Port:    " + s.getPort());
                }, () -> sender.sendMessage("Service not found: " + args[1]));
            }
            default -> sender.sendMessage("Usage: " + getUsage());
        }
    }
}