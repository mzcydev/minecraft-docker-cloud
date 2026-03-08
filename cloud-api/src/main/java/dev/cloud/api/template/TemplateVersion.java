package dev.cloud.api.template;

import java.time.Instant;

/**
 * Represents a versioned snapshot of a template, identified by a checksum.
 * Used to detect whether a node's local copy is up to date.
 */
public record TemplateVersion(
        String templateName,
        String checksum,
        Instant createdAt,
        long sizeBytes
) {
    /**
     * Returns {@code true} if this version's checksum differs from the given checksum,
     * indicating the template has been updated.
     *
     * @param otherChecksum the checksum to compare against
     */
    public boolean isNewerThan(String otherChecksum) {
        return !checksum.equals(otherChecksum);
    }
}