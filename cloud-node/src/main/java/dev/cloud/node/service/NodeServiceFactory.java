package dev.cloud.node.service;

import dev.cloud.api.group.ServiceGroup;
import dev.cloud.api.service.CloudServiceImpl;
import dev.cloud.api.service.ServiceState;
import dev.cloud.networking.service.ServiceReporterClient;
import dev.cloud.node.NodeConfig;
import dev.cloud.node.docker.NodeDockerService;
import dev.cloud.node.template.NodeTemplateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.UUID;

public class NodeServiceFactory {

    private static final Logger log = LoggerFactory.getLogger(NodeServiceFactory.class);

    private final NodeDockerService dockerService;
    private final NodeTemplateManager templateManager;
    private final ServiceDirectoryManager directoryManager;
    private final ServiceReporterClient reporterClient;
    private final NodeConfig config;

    public NodeServiceFactory(NodeDockerService dockerService,
                              NodeTemplateManager templateManager,
                              ServiceDirectoryManager directoryManager,
                              ServiceReporterClient reporterClient,
                              NodeConfig config) {
        this.dockerService    = dockerService;
        this.templateManager  = templateManager;
        this.directoryManager = directoryManager;
        this.reporterClient   = reporterClient;
        this.config           = config;
    }

    public RunningService create(String serviceName, ServiceGroup group) throws Exception {
        UUID serviceId = UUID.randomUUID();

        Path workDir = directoryManager.create(serviceName);
        templateManager.prepareForService(group.getTemplateName(), workDir);

        NodeDockerService.StartResult result = dockerService.start(serviceName, group, workDir);

        CloudServiceImpl cloudService = new CloudServiceImpl(
                serviceId, serviceName, group.getName(),
                config.nodeName(), config.host(),
                result.port(), group.getMaxPlayers(),
                ServiceState.STARTING, result.containerId()
        );

        reporterClient.reportStateChange(serviceName, ServiceState.STARTING);

        log.info("Service '{}' starting on port {}.", serviceName, result.port());
        return new RunningService(cloudService, result.containerId(), result.port(), result.logStream());
    }

    public NodeDockerService dockerService() { return dockerService; }
    public ServiceDirectoryManager directoryManager() { return directoryManager; }
}
