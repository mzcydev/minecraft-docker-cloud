package dev.cloud.master;

import dev.cloud.master.group.MasterGroupManager;
import dev.cloud.master.node.MasterNodeManager;
import dev.cloud.master.node.NodeRegistry;
import dev.cloud.master.player.GlobalPlayerRegistry;
import dev.cloud.master.player.MasterPlayerManager;
import dev.cloud.master.service.MasterServiceManager;
import dev.cloud.master.service.ServiceRegistry;
import dev.cloud.master.template.MasterTemplateManager;

/**
 * Central façade that exposes all master subsystems.
 * Passed into gRPC services, REST handlers and console commands
 * to avoid threading individual managers everywhere.
 */
public class MasterCloudAPI {

    private final MasterGroupManager groupManager;
    private final MasterNodeManager nodeManager;
    private final NodeRegistry nodeRegistry;
    private final MasterServiceManager serviceManager;
    private final ServiceRegistry serviceRegistry;
    private final MasterPlayerManager playerManager;
    private final GlobalPlayerRegistry playerRegistry;
    private final MasterTemplateManager templateManager;
    private final MasterConfig config;

    public MasterCloudAPI(MasterGroupManager groupManager,
                          MasterNodeManager nodeManager,
                          NodeRegistry nodeRegistry,
                          MasterServiceManager serviceManager,
                          ServiceRegistry serviceRegistry,
                          MasterPlayerManager playerManager,
                          GlobalPlayerRegistry playerRegistry,
                          MasterTemplateManager templateManager,
                          MasterConfig config) {
        this.groupManager = groupManager;
        this.nodeManager = nodeManager;
        this.nodeRegistry = nodeRegistry;
        this.serviceManager = serviceManager;
        this.serviceRegistry = serviceRegistry;
        this.playerManager = playerManager;
        this.playerRegistry = playerRegistry;
        this.templateManager = templateManager;
        this.config = config;
    }

    public MasterGroupManager groupManager() {
        return groupManager;
    }

    public MasterNodeManager nodeManager() {
        return nodeManager;
    }

    public NodeRegistry nodeRegistry() {
        return nodeRegistry;
    }

    public MasterServiceManager serviceManager() {
        return serviceManager;
    }

    public ServiceRegistry serviceRegistry() {
        return serviceRegistry;
    }

    public MasterPlayerManager playerManager() {
        return playerManager;
    }

    public GlobalPlayerRegistry playerRegistry() {
        return playerRegistry;
    }

    public MasterTemplateManager templateManager() {
        return templateManager;
    }

    public MasterConfig config() {
        return config;
    }
}