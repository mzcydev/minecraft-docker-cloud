package dev.cloud.master.scheduler;

import dev.cloud.master.node.MasterNodeManager;
import dev.cloud.master.service.MasterServiceManager;
import dev.cloud.master.service.ServiceQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Drains the {@link ServiceQueue} by attempting to start pending services
 * when nodes are available. Runs every 5 seconds via the {@link TaskExecutor}.
 */
public class ServiceScheduler implements SchedulerTask {

    private static final Logger log = LoggerFactory.getLogger(ServiceScheduler.class);

    private final ServiceQueue queue;
    private final MasterServiceManager serviceManager;
    private final MasterNodeManager nodeManager;

    public ServiceScheduler(ServiceQueue queue,
                            MasterServiceManager serviceManager,
                            MasterNodeManager nodeManager) {
        this.queue = queue;
        this.serviceManager = serviceManager;
        this.nodeManager = nodeManager;
    }

    @Override
    public String getTaskName() {
        return "service-scheduler";
    }

    @Override
    public void run() {
        if (!queue.hasPending() || nodeManager.connectedCount() == 0) return;

        var group = queue.poll();
        while (group != null) {
            var result = serviceManager.startService(group);
            if (result.isEmpty()) {
                // No node available — re-queue and stop trying
                queue.enqueue(group);
                log.debug("Re-queued group '{}' — no node available.", group.getName());
                break;
            }
            group = queue.poll();
        }
    }
}