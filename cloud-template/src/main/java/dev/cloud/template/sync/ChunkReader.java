package dev.cloud.template.sync;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Splits a file into fixed-size byte chunks for streaming over gRPC.
 * Used by the master when sending template files to nodes.
 */
public class ChunkReader {

    /** Default chunk size: 512 KB. */
    public static final int DEFAULT_CHUNK_SIZE = 512 * 1024;

    private final int chunkSize;

    public ChunkReader() {
        this(DEFAULT_CHUNK_SIZE);
    }

    /**
     * @param chunkSize the maximum size of each chunk in bytes
     */
    public ChunkReader(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    /**
     * Reads a file and splits it into a list of byte arrays.
     * The last chunk may be smaller than {@code chunkSize}.
     *
     * @param file the file to read
     * @return ordered list of byte array chunks
     * @throws IOException if the file cannot be read
     */
    public List<byte[]> read(Path file) throws IOException {
        List<byte[]> chunks = new ArrayList<>();
        try (InputStream in = Files.newInputStream(file)) {
            byte[] buffer = new byte[chunkSize];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                byte[] chunk = new byte[bytesRead];
                System.arraycopy(buffer, 0, chunk, 0, bytesRead);
                chunks.add(chunk);
            }
        }
        return chunks;
    }

    /**
     * Returns the total number of chunks a file would produce without reading it fully.
     *
     * @param fileSize the file size in bytes
     * @return the number of chunks
     */
    public int chunkCount(long fileSize) {
        return (int) Math.ceil((double) fileSize / chunkSize);
    }
}