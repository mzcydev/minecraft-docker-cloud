package dev.cloud.node.service;

import dev.cloud.api.group.ServiceGroup;
import dev.cloud.api.service.CloudServiceImpl;
import dev.cloud.api.service.ServiceState;
import dev.cloud.networking.service.ServiceRpcClient;
import dev.cloud.node.config.NodeConfig;
import dev.cloud.node.docker.NodeDockerService;
import dev.cloud.node.template.NodeTemplateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.UUID;

/**
 * Creates new {@link RunningService} instances by:
 * 1. Preparing the service directory
 * 2. Syncing the template
 * 3. Starting the Docker container
 * 4. Reporting the started state to the master
 */
public class NodeServiceFactory {

    private static final Logger log = LoggerFactory.getLogger(NodeServiceFactory.class);

    private final NodeDockerService dockerService;
    private final NodeTemplateManager templateManager;
    private final ServiceDirectoryManager directoryManager;
    private final ServiceRpcClient serviceRpcClient;
    private final NodeConfig config;

    public NodeServiceFactory(NodeDockerService dockerService,
                              NodeTemplateManager templateManager,
                              ServiceDirectoryManager directoryManager,
                              ServiceRpcClient serviceRpcClient,
                              NodeConfig config) {
        this.dockerService = dockerService;
        this.templateManager = templateManager;
        this.directoryManager = directoryManager;
        this.serviceRpcClient = serviceRpcClient;
        this.config = config;
    }

    /**
     * Creates and starts a new service for the given group.
     *
     * @param serviceName the unique name for this service instance
     * @param group       the group configuration to use
     * @return the started {@link RunningService}
     * @throws Exception if any step fails
     */
    public RunningService create(String serviceName, ServiceGroup group) throws Exception {
        UUID serviceId = UUID.randomUUID();

        // 1. Create working directory
        Path workDir = directoryManager.create(serviceName);

        // 2. Sync template
        templateManager.prepareForService(group.getTemplateName(), workDir);

        // 3. Start Docker container
        NodeDockerService.StartResult result = dockerService.start(serviceName, group, workDir);

        // 4. Build CloudService
        CloudServiceImpl cloudService = new CloudServiceImpl(
                serviceId, serviceName, group.getName(),
                config.nodeName(), config.host(),
                result.port(), group.getMaxPlayers(),
                ServiceState.STARTING, result.containerId()
        );

        // 5. Report to master
        serviceRpcClient.reportStateChange(cloudService.getId().toString(),
                serviceName, ServiceState.STARTING);

        log.info("Service '{}' created and starting on port {}.", serviceName, result.port());
        return new RunningService(cloudService, result.containerId(), result.port(), result.logStream());
    }
}