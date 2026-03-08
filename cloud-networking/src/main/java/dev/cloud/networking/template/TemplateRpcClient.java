package dev.cloud.networking.template;

import dev.cloud.proto.template.*;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Node-side stub wrapper for calling the master's {@link TemplateServiceGrpc}.
 * Used by nodes to check if their local template cache is stale and to download updates.
 */
public class TemplateRpcClient {

    private static final Logger log = LoggerFactory.getLogger(TemplateRpcClient.class);

    private final TemplateServiceGrpc.TemplateServiceBlockingStub stub;

    /**
     * @param channel the open channel to the master
     */
    public TemplateRpcClient(ManagedChannel channel) {
        this.stub = TemplateServiceGrpc.newBlockingStub(channel);
    }

    /**
     * Returns metadata for all templates registered on the master.
     */
    public List<ProtoTemplate> listTemplates() {
        return stub.listTemplates(dev.cloud.proto.common.Empty.getDefaultInstance()).getTemplatesList();
    }

    /**
     * Checks whether the node's local copy of a template is up to date.
     *
     * @param templateName  the template to check
     * @param localChecksum the MD5/SHA checksum of the node's local copy, or empty string if not cached
     * @return the master's sync response indicating whether a download is needed
     */
    public TemplateSyncResponse checkSync(String templateName, String localChecksum) {
        return stub.checkSync(TemplateSyncRequest.newBuilder()
                .setTemplateName(templateName)
                .setLocalChecksum(localChecksum)
                .build());
    }

    /**
     * Downloads a template from the master and writes all files into the given target directory.
     * Creates the directory and any necessary parent directories if they do not exist.
     *
     * @param templateName the name of the template to download
     * @param targetDir    the local directory to write the template files into
     * @throws IOException if any file cannot be written
     */
    public void downloadTemplate(String templateName, Path targetDir) throws IOException {
        log.info("Downloading template '{}' to {}", templateName, targetDir);
        Files.createDirectories(targetDir);

        // Buffer chunks per file path until all chunks of a file are received
        Map<String, Map<Integer, byte[]>> fileChunks = new HashMap<>();
        Map<String, Integer> fileTotalChunks = new HashMap<>();

        Iterator<TemplateChunk> it = stub.downloadTemplate(
                GetTemplateRequest.newBuilder().setTemplateName(templateName).build()
        );

        while (it.hasNext()) {
            TemplateChunk chunk = it.next();
            String path = chunk.getRelativePath();

            fileChunks.computeIfAbsent(path, k -> new HashMap<>())
                    .put(chunk.getChunkIndex(), chunk.getData().toByteArray());
            fileTotalChunks.put(path, chunk.getTotalChunks());

            if (chunk.getLastChunk()) {
                writeFile(targetDir, path, fileChunks.get(path), fileTotalChunks.get(path));
                fileChunks.remove(path);
            }
        }

        log.info("Template '{}' downloaded successfully.", templateName);
    }

    private void writeFile(Path baseDir, String relativePath,
                           Map<Integer, byte[]> chunks, int totalChunks) throws IOException {
        Path target = baseDir.resolve(relativePath);
        Files.createDirectories(target.getParent());

        try (OutputStream out = Files.newOutputStream(target)) {
            for (int i = 0; i < totalChunks; i++) {
                byte[] data = chunks.get(i);
                if (data != null) out.write(data);
            }
        }

        log.debug("Written template file: {}", relativePath);
    }
}