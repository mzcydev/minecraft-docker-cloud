package dev.cloud.master.template;

import dev.cloud.api.template.TemplateVersion;
import dev.cloud.template.storage.LocalTemplateStorage;
import dev.cloud.template.sync.ChunkReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Master-side template manager.
 * Provides the gRPC {@code TemplateRpcService} with access to stored templates
 * and serves template data as chunks for streaming to nodes.
 */
public class MasterTemplateManager {

    private static final Logger log = LoggerFactory.getLogger(MasterTemplateManager.class);

    private final LocalTemplateStorage storage;
    private final ChunkReader chunkReader = new ChunkReader();

    public MasterTemplateManager(LocalTemplateStorage storage) {
        this.storage = storage;
    }

    /**
     * Returns all template names available on the master.
     *
     * @throws IOException if the template directory cannot be listed
     */
    public List<String> listTemplateNames() throws IOException {
        return storage.listTemplateNames();
    }

    /**
     * Returns the current version (checksum + size) of a template.
     *
     * @param templateName the template to check
     * @throws IOException if the template cannot be read
     */
    public TemplateVersion getVersion(String templateName) throws IOException {
        return storage.computeVersion(templateName);
    }

    /**
     * Returns the filesystem path of a template directory.
     *
     * @param templateName the template name
     * @return the absolute path to the template directory
     */
    public Path getTemplatePath(String templateName) {
        return storage.getTemplatePath(templateName);
    }

    /**
     * Reads a single file from a template and returns it as ordered chunks.
     * Used by the gRPC streaming handler to send the file to a node.
     *
     * @param templateName the template containing the file
     * @param relativePath the file path relative to the template root
     * @return ordered list of byte array chunks
     * @throws IOException if the file cannot be read
     */
    public List<byte[]> readFileChunks(String templateName, String relativePath) throws IOException {
        Path file = storage.getTemplatePath(templateName).resolve(relativePath);
        return chunkReader.read(file);
    }
}