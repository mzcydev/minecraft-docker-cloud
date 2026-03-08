package dev.cloud.node.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Manages the working directories for services running on this node.
 * Each service gets its own directory under the configured work root.
 */
public class ServiceDirectoryManager {

    private static final Logger log = LoggerFactory.getLogger(ServiceDirectoryManager.class);

    private final Path workRoot;

    /**
     * @param workRoot the root directory for all service working directories
     *                 (e.g. {@code Path.of("services/")})
     */
    public ServiceDirectoryManager(Path workRoot) {
        this.workRoot = workRoot;
        try {
            Files.createDirectories(workRoot);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create service work root: " + workRoot, e);
        }
    }

    /**
     * Creates and returns the working directory for a new service.
     *
     * @param serviceName the service name (e.g. {@code "Lobby-1"})
     * @return the path to the service's working directory
     */
    public Path create(String serviceName) throws IOException {
        Path dir = workRoot.resolve(serviceName);
        Files.createDirectories(dir);
        log.debug("Service directory created: {}", dir);
        return dir;
    }

    /**
     * Returns the working directory path for an existing service.
     *
     * @param serviceName the service name
     * @return the path to the service's directory
     */
    public Path get(String serviceName) {
        return workRoot.resolve(serviceName);
    }

    /**
     * Deletes the working directory for a stopped service.
     * Recursively removes all files and subdirectories.
     *
     * @param serviceName the service whose directory should be deleted
     */
    public void delete(String serviceName) {
        Path dir = workRoot.resolve(serviceName);
        if (!Files.exists(dir)) return;
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path d, IOException exc) throws IOException {
                    Files.delete(d);
                    return FileVisitResult.CONTINUE;
                }
            });
            log.debug("Service directory deleted: {}", dir);
        } catch (IOException e) {
            log.warn("Failed to delete service directory '{}': {}", dir, e.getMessage());
        }
    }
}