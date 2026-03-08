package dev.cloud.template.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Template storage backend that reads and writes template files from an S3-compatible
 * object store (AWS S3, MinIO, etc.).
 * Templates are stored as objects with keys prefixed by the template name.
 */
public class S3TemplateStorage {

    private static final Logger log = LoggerFactory.getLogger(S3TemplateStorage.class);

    private final S3Client s3;
    private final String bucket;
    private final String keyPrefix;

    /**
     * @param s3        a configured {@link S3Client} (works with AWS S3 and MinIO)
     * @param bucket    the S3 bucket name
     * @param keyPrefix the key prefix for all template objects (e.g. {@code "templates/"})
     */
    public S3TemplateStorage(S3Client s3, String bucket, String keyPrefix) {
        this.s3 = s3;
        this.bucket = bucket;
        this.keyPrefix = keyPrefix.endsWith("/") ? keyPrefix : keyPrefix + "/";
    }

    /**
     * Downloads all objects belonging to a template into a local directory.
     *
     * @param templateName the name of the template to download
     * @param targetDir    the local directory to write files into
     * @throws IOException if any file operation fails
     */
    public void download(String templateName, Path targetDir) throws IOException {
        String prefix = keyPrefix + templateName + "/";
        Files.createDirectories(targetDir);

        ListObjectsV2Response response = s3.listObjectsV2(ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefix)
                .build());

        for (S3Object obj : response.contents()) {
            String relativePath = obj.key().substring(prefix.length());
            if (relativePath.isBlank()) continue;

            Path targetFile = targetDir.resolve(relativePath);
            Files.createDirectories(targetFile.getParent());

            s3.getObject(
                    GetObjectRequest.builder().bucket(bucket).key(obj.key()).build(),
                    ResponseTransformer.toFile(targetFile)
            );
            log.debug("Downloaded s3://{}/{} → {}", bucket, obj.key(), targetFile);
        }

        log.info("Template '{}' downloaded from S3 to {}", templateName, targetDir);
    }

    /**
     * Uploads all files from a local directory as S3 objects for the given template.
     *
     * @param templateName the name of the template
     * @param sourceDir    the local directory containing the template files
     * @throws IOException if any file operation fails
     */
    public void upload(String templateName, Path sourceDir) throws IOException {
        String prefix = keyPrefix + templateName + "/";

        List<Path> files = Files.walk(sourceDir)
                .filter(Files::isRegularFile)
                .toList();

        for (Path file : files) {
            String relativePath = sourceDir.relativize(file).toString().replace("\\", "/");
            String key = prefix + relativePath;

            s3.putObject(
                    PutObjectRequest.builder().bucket(bucket).key(key).build(),
                    RequestBody.fromFile(file)
            );
            log.debug("Uploaded {} → s3://{}/{}", file, bucket, key);
        }

        log.info("Template '{}' uploaded to S3 ({} files)", templateName, files.size());
    }

    /**
     * Deletes all S3 objects belonging to the given template.
     *
     * @param templateName the name of the template to delete
     */
    public void delete(String templateName) {
        String prefix = keyPrefix + templateName + "/";

        ListObjectsV2Response response = s3.listObjectsV2(ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefix)
                .build());

        List<ObjectIdentifier> toDelete = response.contents().stream()
                .map(obj -> ObjectIdentifier.builder().key(obj.key()).build())
                .toList();

        if (toDelete.isEmpty()) return;

        s3.deleteObjects(DeleteObjectsRequest.builder()
                .bucket(bucket)
                .delete(Delete.builder().objects(toDelete).build())
                .build());

        log.info("Template '{}' deleted from S3 ({} objects removed)", templateName, toDelete.size());
    }
}