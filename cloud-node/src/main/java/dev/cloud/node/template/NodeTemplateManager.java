package dev.cloud.node.template;

import dev.cloud.template.storage.LocalTemplateStorage;
import dev.cloud.template.sync.TemplateSyncer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Manages the node's local template cache.
 * Downloads templates from the master if needed and copies them into service directories.
 */
public class NodeTemplateManager {

    private static final Logger log = LoggerFactory.getLogger(NodeTemplateManager.class);

    private final LocalTemplateStorage storage;
    private final TemplateSyncer syncer;
    private final TemplateReceiver receiver;

    public NodeTemplateManager(LocalTemplateStorage storage,
                               TemplateSyncer syncer,
                               TemplateReceiver receiver) {
        this.storage = storage;
        this.syncer = syncer;
        this.receiver = receiver;
    }

    /**
     * Ensures the given template is available locally and copies it into the service directory.
     * If the template is not cached or outdated, it is downloaded from the master first.
     *
     * @param templateName the name of the template to prepare
     * @param serviceDir   the service working directory to copy the template into
     * @throws IOException if any file operation fails
     */
    public void prepareForService(String templateName, Path serviceDir) throws IOException {
        // Download from master if needed
        receiver.receive(templateName);

        // Full copy into service directory
        syncer.fullCopy(templateName, serviceDir);
        log.info("Template '{}' prepared in {}", templateName, serviceDir);
    }

    /**
     * Forces a re-download of the given template from the master.
     *
     * @param templateName the template to refresh
     * @throws IOException if the download fails
     */
    public void refresh(String templateName) throws IOException {
        receiver.receive(templateName);
        log.info("Template '{}' refreshed from master.", templateName);
    }
}