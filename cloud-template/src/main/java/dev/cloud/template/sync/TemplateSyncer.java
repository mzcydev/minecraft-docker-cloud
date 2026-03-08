package dev.cloud.template.sync;

import dev.cloud.template.storage.LocalTemplateStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Coordinates template synchronization from the master's local storage to a target directory.
 * Uses {@link TemplateDiff} to only transfer changed files.
 */
public class TemplateSyncer {

    private static final Logger log = LoggerFactory.getLogger(TemplateSyncer.class);

    private final LocalTemplateStorage storage;
    private final TemplateDiff diff;

    /**
     * @param storage the local template storage on the master
     */
    public TemplateSyncer(LocalTemplateStorage storage) {
        this.storage = storage;
        this.diff = new TemplateDiff();
    }

    /**
     * Synchronizes a template to the target directory.
     * Only files that are missing or have changed are copied.
     * Files in the target that no longer exist in the template are deleted.
     *
     * @param templateName the name of the template to sync
     * @param target       the target directory to sync into
     * @throws IOException if any file operation fails
     */
    public void sync(String templateName, Path target) throws IOException {
        Path source = storage.getTemplatePath(templateName);

        if (!Files.exists(source)) {
            throw new IllegalArgumentException("Template not found: " + templateName);
        }

        TemplateDiff.DiffResult result = diff.diff(source, target);

        if (result.isEmpty()) {
            log.debug("Template '{}' is already up to date at {}", templateName, target);
            return;
        }

        log.info("Syncing template '{}': {} to transfer, {} to delete",
                templateName, result.toTransfer().size(), result.toDelete().size());

        // Copy changed/new files
        for (String relativePath : result.toTransfer()) {
            Path sourceFile = source.resolve(relativePath);
            Path targetFile = target.resolve(relativePath);
            Files.createDirectories(targetFile.getParent());
            Files.copy(sourceFile, targetFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        // Delete removed files
        for (String relativePath : result.toDelete()) {
            Path targetFile = target.resolve(relativePath);
            Files.deleteIfExists(targetFile);
            log.debug("Deleted stale file: {}", relativePath);
        }

        log.info("Template '{}' synced successfully.", templateName);
    }

    /**
     * Performs a full copy of the template into the target directory without diffing.
     * Faster than {@link #sync} for fresh service directories.
     *
     * @param templateName the template to copy
     * @param target       the target directory
     * @throws IOException if any file operation fails
     */
    public void fullCopy(String templateName, Path target) throws IOException {
        storage.copyTo(templateName, target);
        log.info("Full copy of template '{}' to {}", templateName, target);
    }
}