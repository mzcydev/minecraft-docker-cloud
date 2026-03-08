package dev.cloud.template.storage;

import dev.cloud.api.template.Template;
import dev.cloud.api.template.TemplateImpl;
import dev.cloud.api.template.TemplateStorage;
import dev.cloud.api.template.TemplateVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

/**
 * Template storage backend that reads and writes template files from the local filesystem.
 * Templates are stored as directories under a configurable root path.
 */
public class LocalTemplateStorage {

    private static final Logger log = LoggerFactory.getLogger(LocalTemplateStorage.class);

    private final Path root;

    /**
     * @param root the root directory where all templates are stored
     *             (e.g. {@code Path.of("templates")})
     */
    public LocalTemplateStorage(Path root) {
        this.root = root;
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create template root directory: " + root, e);
        }
    }

    /**
     * Returns the filesystem path of a template directory.
     *
     * @param templateName the template name
     * @return the path to the template's root directory
     */
    public Path getTemplatePath(String templateName) {
        return root.resolve(templateName);
    }

    /**
     * Creates an empty template directory if it does not already exist.
     *
     * @param templateName the name of the template to create
     * @return a {@link Template} representing the new template
     */
    public Template createTemplate(String templateName) throws IOException {
        Path templateDir = getTemplatePath(templateName);
        Files.createDirectories(templateDir);
        log.info("Template directory created: {}", templateDir);
        return new TemplateImpl(templateName, TemplateStorage.LOCAL, templateDir.toString());
    }

    /**
     * Copies all files from the template directory into the given target directory.
     * Existing files in the target are overwritten.
     *
     * @param templateName the name of the template to copy
     * @param target       the destination directory (e.g. a service's working directory)
     * @throws IOException if any file operation fails
     */
    public void copyTo(String templateName, Path target) throws IOException {
        Path templateDir = getTemplatePath(templateName);

        if (!Files.exists(templateDir)) {
            throw new IllegalArgumentException("Template not found: " + templateName);
        }

        Files.createDirectories(target);

        Files.walkFileTree(templateDir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path relative = templateDir.relativize(dir);
                Files.createDirectories(target.resolve(relative));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path relative = templateDir.relativize(file);
                Files.copy(file, target.resolve(relative), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });

        log.info("Template '{}' copied to {}", templateName, target);
    }

    /**
     * Computes a version snapshot for the given template by calculating a SHA-256 checksum
     * over all files in the template directory.
     *
     * @param templateName the name of the template
     * @return a {@link TemplateVersion} with current checksum, size and timestamp
     * @throws IOException if the template directory cannot be read
     */
    public TemplateVersion computeVersion(String templateName) throws IOException {
        Path templateDir = getTemplatePath(templateName);
        List<Path> files = Files.walk(templateDir)
                .filter(Files::isRegularFile)
                .sorted()
                .toList();

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            long totalSize = 0;

            for (Path file : files) {
                totalSize += Files.size(file);
                try (InputStream in = new DigestInputStream(Files.newInputStream(file), digest)) {
                    in.transferTo(OutputStream.nullOutputStream());
                }
            }

            String checksum = HexFormat.of().formatHex(digest.digest());
            return new TemplateVersion(templateName, checksum, Instant.now(), totalSize);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * Returns all template names found in the root directory.
     *
     * @return list of template names (directory names under root)
     * @throws IOException if the root directory cannot be listed
     */
    public List<String> listTemplateNames() throws IOException {
        if (!Files.exists(root)) return List.of();
        try (var stream = Files.list(root)) {
            return stream
                    .filter(Files::isDirectory)
                    .map(p -> p.getFileName().toString())
                    .toList();
        }
    }
}