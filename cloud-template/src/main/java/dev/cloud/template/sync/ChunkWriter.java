package dev.cloud.template.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

/**
 * Reassembles a file from ordered byte chunks received over gRPC.
 * Buffers chunks in memory and writes them to disk when the last chunk arrives.
 */
public class ChunkWriter {

    private static final Logger log = LoggerFactory.getLogger(ChunkWriter.class);

    private final Path outputDir;

    /**
     * Per-file chunk buffer: relative path → (chunkIndex → data).
     */
    private final Map<String, TreeMap<Integer, byte[]>> buffers = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * @param outputDir the directory to write reassembled files into
     */
    public ChunkWriter(Path outputDir) {
        this.outputDir = outputDir;
    }

    /**
     * Receives a single chunk and buffers it.
     * When the last chunk for a file arrives, the file is written to disk.
     *
     * @param relativePath the file's relative path within the template directory
     * @param chunkIndex   the zero-based index of this chunk
     * @param totalChunks  the total number of chunks for this file
     * @param data         the raw bytes of this chunk
     * @param isLast       {@code true} if this is the final chunk for this file
     * @throws IOException if the file cannot be written when the last chunk arrives
     */
    public void receive(String relativePath, int chunkIndex, int totalChunks,
                        byte[] data, boolean isLast) throws IOException {
        buffers.computeIfAbsent(relativePath, k -> new TreeMap<>())
                .put(chunkIndex, data);

        if (isLast) {
            flush(relativePath, totalChunks);
        }
    }

    /**
     * Writes all buffered chunks for the given file to disk in order.
     *
     * @param relativePath the file to flush
     * @param totalChunks  expected total number of chunks (for validation)
     */
    private void flush(String relativePath, int totalChunks) throws IOException {
        TreeMap<Integer, byte[]> chunks = buffers.remove(relativePath);
        if (chunks == null) return;

        Path target = outputDir.resolve(relativePath);
        Files.createDirectories(target.getParent());

        try (OutputStream out = Files.newOutputStream(target)) {
            for (int i = 0; i < totalChunks; i++) {
                byte[] chunk = chunks.get(i);
                if (chunk != null) out.write(chunk);
            }
        }

        log.debug("Written file from {} chunks: {}", chunks.size(), relativePath);
    }

    /**
     * Returns the number of files currently buffered but not yet flushed.
     */
    public int pendingFileCount() {
        return buffers.size();
    }
}