package dev.cloud.template.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Computes the difference between two directories to determine which files
 * need to be transferred during a template sync.
 * Uses MD5 checksums per file for change detection.
 */
public class TemplateDiff {

    private static final Logger log = LoggerFactory.getLogger(TemplateDiff.class);

    /**
     * Computes the diff between a source and target directory.
     *
     * @param source the authoritative source directory (template on master)
     * @param target the directory to compare against (cached copy on node)
     * @return a {@link DiffResult} describing what needs to change
     * @throws IOException if either directory cannot be read
     */
    public DiffResult diff(Path source, Path target) throws IOException {
        Map<String, String> sourceChecksums = checksumDirectory(source);
        Map<String, String> targetChecksums = Files.exists(target)
                ? checksumDirectory(target)
                : Map.of();

        Set<String> toTransfer = sourceChecksums.entrySet().stream()
                .filter(e -> !e.getValue().equals(targetChecksums.get(e.getKey())))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        Set<String> toDelete = targetChecksums.keySet().stream()
                .filter(path -> !sourceChecksums.containsKey(path))
                .collect(Collectors.toSet());

        log.debug("Diff: {} files to transfer, {} files to delete", toTransfer.size(), toDelete.size());
        return new DiffResult(toTransfer, toDelete);
    }

    /**
     * Computes MD5 checksums for all files in a directory, keyed by relative path.
     *
     * @param dir the directory to checksum
     * @return map of relative path → MD5 hex string
     */
    private Map<String, String> checksumDirectory(Path dir) throws IOException {
        Map<String, String> checksums = new HashMap<>();

        Files.walkFileTree(dir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String relativePath = dir.relativize(file).toString().replace("\\", "/");
                checksums.put(relativePath, md5(file));
                return FileVisitResult.CONTINUE;
            }
        });

        return checksums;
    }

    private String md5(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(Files.readAllBytes(file));
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not available", e);
        }
    }

    /**
     * Result of a diff operation containing the files that need to be transferred or deleted.
     */
    public record DiffResult(
            /** Files present in source but missing or changed in target. */
            Set<String> toTransfer,
            /** Files present in target but no longer in source. */
            Set<String> toDelete
    ) {
        /**
         * Returns {@code true} if there are no changes between source and target.
         */
        public boolean isEmpty() {
            return toTransfer.isEmpty() && toDelete.isEmpty();
        }
    }
}