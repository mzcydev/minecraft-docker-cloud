package dev.cloud.master;

import dev.cloud.api.event.EventBusImpl;
import dev.cloud.master.group.MasterGroupManager;
import dev.cloud.master.node.MasterNodeManager;
import dev.cloud.master.node.NodeRegistry;
import dev.cloud.master.player.GlobalPlayerRegistry;
import dev.cloud.master.player.MasterPlayerManager;
import dev.cloud.master.service.MasterServiceManager;
import dev.cloud.master.service.ServiceRegistry;
import dev.cloud.master.service.ServiceScaler;
import dev.cloud.master.template.MasterTemplateManager;
import dev.cloud.networking.GrpcChannelManager;
import dev.cloud.networking.GrpcServerBootstrap;
import dev.cloud.networking.group.GroupRpcService;
import dev.cloud.networking.node.NodeRpcService;
import dev.cloud.networking.player.PlayerRpcService;
import dev.cloud.networking.service.ServiceRpcService;
import dev.cloud.networking.template.TemplateRpcService;
import dev.cloud.template.storage.LocalTemplateStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class MasterBootstrap {

    private static final Logger log = LoggerFactory.getLogger(MasterBootstrap.class);

    private final MasterConfig config;
    private GrpcServerBootstrap grpcServer;
    private ServiceScaler serviceScaler;

    public MasterBootstrap(MasterConfig config) {
        this.config = config;
    }

    public void start() throws Exception {
        EventBusImpl eventBus = new EventBusImpl();
        GrpcChannelManager channelManager = new GrpcChannelManager();

        LocalTemplateStorage templateStorage = new LocalTemplateStorage(Path.of(config.templateDir()));
        MasterTemplateManager templateManager = new MasterTemplateManager(templateStorage);

        NodeRegistry nodeRegistry = new NodeRegistry();
        ServiceRegistry serviceRegistry = new ServiceRegistry();
        GlobalPlayerRegistry playerRegistry = new GlobalPlayerRegistry();

        MasterNodeManager nodeManager = new MasterNodeManager(eventBus, channelManager);
        MasterGroupManager groupManager = new MasterGroupManager(eventBus);
        MasterServiceManager serviceManager = new MasterServiceManager(
                eventBus, nodeManager, channelManager, config);
        MasterPlayerManager playerManager = new MasterPlayerManager(eventBus);

        MasterCloudAPI cloudAPI = new MasterCloudAPI(
                groupManager, nodeManager, nodeRegistry,
                serviceManager, serviceRegistry,
                playerManager, playerRegistry,
                templateManager, config
        );

        serviceScaler = new ServiceScaler(groupManager, serviceManager, nodeManager);
        serviceScaler.start();

        grpcServer = new GrpcServerBootstrap(config.grpcPort(), config.authToken());
        grpcServer.addService(new NodeRpcService(nodeManager, channelManager, eventBus));
        grpcServer.addService(new ServiceRpcService(serviceManager, eventBus));
        grpcServer.addService(new GroupRpcService(groupManager));
        grpcServer.addService(new PlayerRpcService(playerManager, eventBus));
        grpcServer.addService(new TemplateRpcService(templateManager));
        grpcServer.start();

        log.info("Master started on gRPC :{}", config.grpcPort());
    }

    public void shutdown() {
        log.info("Shutting down master...");
        if (serviceScaler != null) serviceScaler.stop();
        if (grpcServer != null) grpcServer.stop();
        log.info("Master shutdown complete.");
    }
}
