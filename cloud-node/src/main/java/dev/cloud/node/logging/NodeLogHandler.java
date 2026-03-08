package dev.cloud.node.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Receives log lines from running service containers and writes them to
 * per-service log files under {@code logs/<serviceName>.log}.
 * Also forwards each line to SLF4J at DEBUG level.
 */
public class NodeLogHandler {

    private static final Logger log = LoggerFactory.getLogger(NodeLogHandler.class);
    private static final Path LOG_DIR = Path.of("logs");

    private final Map<String, Path> logFiles = new ConcurrentHashMap<>();

    public NodeLogHandler() {
        try {
            Files.createDirectories(LOG_DIR);
        } catch (IOException e) {
            log.error("Failed to create log directory: {}", e.getMessage());
        }
    }

    /**
     * Handles a single log line from a service container.
     *
     * @param serviceName the originating service
     * @param line        the log line (already stripped of trailing whitespace)
     */
    public void handle(String serviceName, String line) {
        log.debug("[{}] {}", serviceName, line);
        writeToFile(serviceName, line);
    }

    private void writeToFile(String serviceName, String line) {
        Path logFile = logFiles.computeIfAbsent(serviceName,
                name -> LOG_DIR.resolve(name + ".log"));
        try {
            Files.writeString(logFile, line + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            log.warn("Failed to write log for service '{}': {}", serviceName, e.getMessage());
        }
    }

    /**
     * Closes the log file handle for a service that has stopped.
     *
     * @param serviceName the service whose log should be closed
     */
    public void close(String serviceName) {
        logFiles.remove(serviceName);
    }
}