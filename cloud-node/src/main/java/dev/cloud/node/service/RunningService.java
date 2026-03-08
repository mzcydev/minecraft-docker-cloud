package dev.cloud.node.service;

import dev.cloud.api.service.CloudService;

import java.io.Closeable;

/**
 * Represents a cloud service that is currently running on this node.
 * Holds a reference to the container log stream so it can be closed on stop.
 */
public record RunningService(
        CloudService cloudService,
        String containerId,
        int port,
        Closeable logStream
) {
    /**
     * Closes the container log stream.
     * Called when the service is stopped.
     */
    public void closeLog() {
        try {
            if (logStream != null) logStream.close();
        } catch (Exception ignored) {}
    }
}