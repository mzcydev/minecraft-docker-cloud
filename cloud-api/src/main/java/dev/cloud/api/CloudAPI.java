package dev.cloud.api;

import dev.cloud.api.event.EventBus;
import dev.cloud.api.group.GroupManager;
import dev.cloud.api.node.NodeManager;
import dev.cloud.api.player.PlayerManager;
import dev.cloud.api.service.ServiceManager;
import dev.cloud.api.template.TemplateManager;

/**
 * Central access point for all cloud system components.
 * Holds references to all managers and is accessible as a singleton.
 */
public class CloudAPI {

    private static CloudAPI instance;

    private final ServiceManager serviceManager;
    private final GroupManager groupManager;
    private final NodeManager nodeManager;
    private final PlayerManager playerManager;
    private final TemplateManager templateManager;
    private final EventBus eventBus;

    public CloudAPI(ServiceManager serviceManager, GroupManager groupManager,
                    NodeManager nodeManager, PlayerManager playerManager,
                    TemplateManager templateManager, EventBus eventBus) {
        this.serviceManager = serviceManager;
        this.groupManager = groupManager;
        this.nodeManager = nodeManager;
        this.playerManager = playerManager;
        this.templateManager = templateManager;
        this.eventBus = eventBus;
        instance = this;
    }

    /**
     * Returns the global CloudAPI instance.
     *
     * @throws IllegalStateException if the API has not been initialized yet
     */
    public static CloudAPI getInstance() {
        if (instance == null) throw new IllegalStateException("CloudAPI not initialized yet");
        return instance;
    }

    public ServiceManager getServiceManager() {
        return serviceManager;
    }

    public GroupManager getGroupManager() {
        return groupManager;
    }

    public NodeManager getNodeManager() {
        return nodeManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public TemplateManager getTemplateManager() {
        return templateManager;
    }

    public EventBus getEventBus() {
        return eventBus;
    }
}