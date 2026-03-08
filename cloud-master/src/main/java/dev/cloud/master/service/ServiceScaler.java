package dev.cloud.master.service;

import dev.cloud.api.group.ServiceGroup;
import dev.cloud.master.group.MasterGroupManager;
import dev.cloud.master.node.MasterNodeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Periodically checks each group's running service count against its min/max
 * configuration and starts or stops services to maintain the desired count.
 * Runs every 10 seconds.
 */
public class ServiceScaler {

    private static final Logger log = LoggerFactory.getLogger(ServiceScaler.class);
    private static final int INTERVAL_SECONDS = 10;

    private final MasterGroupManager groupManager;
    private final MasterServiceManager serviceManager;
    private final MasterNodeManager nodeManager;

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "service-scaler");
                t.setDaemon(true);
                return t;
            });

    public ServiceScaler(MasterGroupManager groupManager,
                         MasterServiceManager serviceManager,
                         MasterNodeManager nodeManager) {
        this.groupManager = groupManager;
        this.serviceManager = serviceManager;
        this.nodeManager = nodeManager;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::scale,
                INTERVAL_SECONDS, INTERVAL_SECONDS, TimeUnit.SECONDS);
        log.info("Service scaler started (every {}s).", INTERVAL_SECONDS);
    }

    public void stop() {
        scheduler.shutdownNow();
    }

    private void scale() {
        if (nodeManager.connectedCount() == 0) return;

        for (ServiceGroup group : groupManager.getAllGroups()) {
            try {
                scaleGroup(group);
            } catch (Exception e) {
                log.error("Error scaling group '{}': {}", group.getName(), e.getMessage());
            }
        }
    }

    private void scaleGroup(ServiceGroup group) {
        int running = serviceManager.findByGroup(group.getName()).size();
        int min = group.getMinServices();
        int max = group.getMaxServices();

        if (running < min) {
            int toStart = min - running;
            log.info("Scaling up '{}': starting {} service(s) ({}/{} running).",
                    group.getName(), toStart, running, min);
            for (int i = 0; i < toStart; i++) {
                serviceManager.startService(group);
            }
        } else if (running > max) {
            int toStop = running - max;
            log.info("Scaling down '{}': stopping {} service(s) ({}/{} running).",
                    group.getName(), toStop, running, max);
            serviceManager.findByGroup(group.getName()).stream()
                    .limit(toStop)
                    .forEach(s -> serviceManager.stopService(s.getId().toString()));
        }
    }
}