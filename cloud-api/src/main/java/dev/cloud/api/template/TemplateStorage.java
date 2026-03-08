package dev.cloud.api.template;

/**
 * Defines the storage backend where template files are kept.
 */
public enum TemplateStorage {

    /** Files are stored on the local filesystem of the master. */
    LOCAL,

    /** Files are stored on a remote FTP server. */
    FTP,

    /** Files are stored in an S3-compatible object store (e.g. MinIO, AWS S3). */
    S3
}