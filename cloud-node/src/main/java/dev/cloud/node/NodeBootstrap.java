package dev.cloud.node;

import com.github.dockerjava.api.DockerClient;
import dev.cloud.docker.*;
import dev.cloud.networking.GrpcClientBootstrap;
import dev.cloud.networking.node.NodeRpcClient;
import dev.cloud.networking.service.ServiceReporterClient;
import dev.cloud.networking.template.TemplateRpcClient;
import dev.cloud.node.docker.NodeDockerService;
import dev.cloud.node.health.HealthMonitor;
import dev.cloud.node.health.HeartbeatSender;
import dev.cloud.node.logging.NodeLogHandler;
import dev.cloud.node.service.NodeServiceFactory;
import dev.cloud.node.service.NodeServiceManager;
import dev.cloud.node.service.NodeServiceTracker;
import dev.cloud.node.service.ServiceDirectoryManager;
import dev.cloud.node.template.NodeTemplateManager;
import dev.cloud.node.template.TemplateReceiver;
import dev.cloud.template.storage.LocalTemplateStorage;
import dev.cloud.template.sync.TemplateSyncer;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * Wires together all node subsystems and manages their lifecycle.
 */
public class NodeBootstrap {

    private static final Logger log = LoggerFactory.getLogger(NodeBootstrap.class);

    private final NodeConfig config;

    private ManagedChannel channel;
    private NodeRpcClient nodeRpcClient;
    private NodeServiceManager serviceManager;
    private HeartbeatSender heartbeatSender;
    private HealthMonitor healthMonitor;
    private NodeCloudAPI cloudAPI;

    public NodeBootstrap(NodeConfig config) {
        this.config = config;
    }

    public void start() throws Exception {
        // 1. Docker subsystem
        DockerClientFactory factory = new DockerClientFactory(config.dockerHost());
        DockerClient docker = factory.create();
        if (!factory.testConnection(docker)) {
            throw new IllegalStateException("Cannot reach Docker daemon at: " + config.dockerHost());
        }

        ContainerManager containerManager = new ContainerManager(docker);
        VolumeManager volumeManager = new VolumeManager(docker);
        NetworkManager networkManager = new NetworkManager(docker);
        ImageManager imageManager = new ImageManager(docker);
        PortAllocator portAllocator = new PortAllocator(config.portRangeFrom(), config.portRangeTo());
        ContainerLogStreamer logStreamer = new ContainerLogStreamer(docker);
        NodeLogHandler logHandler = new NodeLogHandler();

        networkManager.ensureNetworkExists();

        NodeDockerService dockerService = new NodeDockerService(
                containerManager, volumeManager, networkManager,
                imageManager, portAllocator, logStreamer, logHandler
        );

        // 2. gRPC channel to master
        GrpcClientBootstrap grpc = new GrpcClientBootstrap(
                config.masterHost(), config.masterPort(), config.authToken());
        channel = grpc.connect();

        nodeRpcClient = new NodeRpcClient(channel);
        ServiceReporterClient reporterClient = new ServiceReporterClient(channel);
        TemplateRpcClient templateRpcClient = new TemplateRpcClient(channel,
                Path.of(config.templateCacheDir()));

        // 3. Template subsystem
        LocalTemplateStorage localStorage = new LocalTemplateStorage(
                Path.of(config.templateCacheDir()));
        TemplateSyncer syncer = new TemplateSyncer(localStorage);
        TemplateReceiver templateReceiver = new TemplateReceiver(templateRpcClient, localStorage);
        NodeTemplateManager templateManager = new NodeTemplateManager(localStorage, syncer, templateReceiver);

        // 4. Service subsystem
        ServiceDirectoryManager directoryManager = new ServiceDirectoryManager(
                Path.of(config.serviceWorkDir()));
        NodeServiceTracker tracker = new NodeServiceTracker();
        NodeServiceFactory serviceFactory = new NodeServiceFactory(
                dockerService, templateManager, directoryManager, reporterClient, config);
        serviceManager = new NodeServiceManager(tracker, serviceFactory, reporterClient);

        // 5. NodeCloudAPI — exposes API to gRPC handlers
        cloudAPI = new NodeCloudAPI(serviceManager, templateManager, config);

        // 6. Node-side gRPC server (receives commands from master)
        NodeGrpcServer grpcServer = new NodeGrpcServer(config.grpcPort(), cloudAPI, config.authToken());
        grpcServer.start();

        // 7. Register with master
        nodeRpcClient.register(
                config.nodeName(), config.host(), config.grpcPort(),
                config.maxMemoryMb(),
                Runtime.getRuntime().availableProcessors(),
                System.getProperty("os.name"),
                System.getProperty("java.version"),
                "unknown",
                config.authToken()
        );

        // 8. Health monitor + heartbeat
        healthMonitor = new HealthMonitor(docker, config.maxMemoryMb());
        heartbeatSender = new HeartbeatSender(nodeRpcClient, config.nodeName(),
                tracker, healthMonitor);
        heartbeatSender.start();

        log.info("Node '{}' started successfully.", config.nodeName());
    }

    public void shutdown() {
        log.info("Shutting down node '{}'...", config.nodeName());

        if (heartbeatSender != null) heartbeatSender.stop();
        if (serviceManager != null) serviceManager.stopAll();

        if (nodeRpcClient != null) {
            try {
                nodeRpcClient.notifyShutdown(config.nodeName(), "graceful shutdown");
            } catch (Exception e) {
                log.warn("Failed to notify master: {}", e.getMessage());
            }
        }

        if (channel != null && !channel.isShutdown()) channel.shutdownNow();

        log.info("Node shutdown complete.");
    }
}