package dev.cloud.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Fetches live CPU and memory statistics from a running Docker container.
 * Uses docker-java's async stats stream and reads a single snapshot.
 */
public class ContainerStats {

    private static final Logger log = LoggerFactory.getLogger(ContainerStats.class);

    private final DockerClient docker;

    public ContainerStats(DockerClient docker) {
        this.docker = docker;
    }

    /**
     * Fetches a single statistics snapshot for the given container.
     * Blocks for up to 3 seconds waiting for the Docker daemon to respond.
     *
     * @param containerId the container to fetch stats for
     * @return a {@link Snapshot} with CPU and memory data, or {@link Snapshot#empty()} on failure
     */
    public Snapshot fetch(String containerId) {
        AtomicReference<Statistics> ref = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        docker.statsCmd(containerId)
                .withNoStream(true)
                .exec(new ResultCallback.Adapter<>() {
                    @Override
                    public void onNext(Statistics stats) {
                        ref.set(stats);
                        latch.countDown();
                    }
                });

        try {
            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Statistics stats = ref.get();
        if (stats == null) return Snapshot.empty();

        return Snapshot.from(stats);
    }

    /**
     * Immutable snapshot of container resource usage at a point in time.
     */
    public record Snapshot(
            long memoryUsageBytes,
            long memoryLimitBytes,
            double cpuPercent
    ) {
        /**
         * Returns an empty snapshot used when stats are unavailable.
         */
        public static Snapshot empty() {
            return new Snapshot(0, 0, 0.0);
        }

        /**
         * Parses a {@link Statistics} object from the Docker API into a {@link Snapshot}.
         *
         * @param stats the raw Docker statistics
         * @return the parsed snapshot
         */
        public static Snapshot from(Statistics stats) {
            long memUsage = 0;
            long memLimit = 0;
            double cpu = 0.0;

            if (stats.getMemoryStats() != null) {
                memUsage = stats.getMemoryStats().getUsage() != null
                        ? stats.getMemoryStats().getUsage() : 0;
                memLimit = stats.getMemoryStats().getLimit() != null
                        ? stats.getMemoryStats().getLimit() : 0;
            }

            if (stats.getCpuStats() != null && stats.getPreCpuStats() != null) {
                cpu = calculateCpuPercent(stats);
            }

            return new Snapshot(memUsage, memLimit, cpu);
        }

        private static double calculateCpuPercent(Statistics stats) {
            try {
                long cpuDelta = stats.getCpuStats().getCpuUsage().getTotalUsage()
                        - stats.getPreCpuStats().getCpuUsage().getTotalUsage();
                long systemDelta = stats.getCpuStats().getSystemCpuUsage()
                        - stats.getPreCpuStats().getSystemCpuUsage();
                int cpuCount = Math.toIntExact(stats.getCpuStats().getOnlineCpus() != null
                        ? stats.getCpuStats().getOnlineCpus() : 1);

                if (systemDelta > 0 && cpuDelta > 0) {
                    return ((double) cpuDelta / systemDelta) * cpuCount * 100.0;
                }
            } catch (Exception ignored) {}
            return 0.0;
        }

        /** Returns memory usage as a percentage of the limit (0–100). */
        public double memoryPercent() {
            if (memoryLimitBytes == 0) return 0;
            return ((double) memoryUsageBytes / memoryLimitBytes) * 100.0;
        }

        /** Returns memory usage in megabytes. */
        public long memoryUsageMb() {
            return memoryUsageBytes / (1024 * 1024);
        }
    }
}