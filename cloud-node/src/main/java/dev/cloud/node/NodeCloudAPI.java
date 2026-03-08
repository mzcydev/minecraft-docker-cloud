package dev.cloud.node;

import dev.cloud.node.service.NodeServiceManager;
import dev.cloud.node.template.NodeTemplateManager;

/**
 * Façade that exposes node subsystems to the gRPC server handlers.
 * Avoids passing individual managers into every gRPC service implementation.
 */
public class NodeCloudAPI {

    private final NodeServiceManager serviceManager;
    private final NodeTemplateManager templateManager;
    private final NodeConfig config;

    public NodeCloudAPI(NodeServiceManager serviceManager,
                        NodeTemplateManager templateManager,
                        NodeConfig config) {
        this.serviceManager = serviceManager;
        this.templateManager = templateManager;
        this.config = config;
    }

    public NodeServiceManager serviceManager() {
        return serviceManager;
    }

    public NodeTemplateManager templateManager() {
        return templateManager;
    }

    public NodeConfig config() {
        return config;
    }
}