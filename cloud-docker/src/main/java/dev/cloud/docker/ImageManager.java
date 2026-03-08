package dev.cloud.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.core.command.PullImageResultCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Manages Docker images — pulling, listing and removing them.
 */
public class ImageManager {

    private static final Logger log = LoggerFactory.getLogger(ImageManager.class);

    private final DockerClient docker;

    public ImageManager(DockerClient docker) {
        this.docker = docker;
    }

    /**
     * Pulls a Docker image from the registry, blocking until the pull completes.
     * If the image already exists locally, the pull is still attempted to check for updates.
     *
     * @param imageName      the full image name including tag (e.g. {@code "eclipse-temurin:21-jre-alpine"})
     * @param timeoutSeconds maximum time to wait for the pull to complete
     * @throws RuntimeException if the pull fails or times out
     */
    public void pull(String imageName, int timeoutSeconds) {
        log.info("Pulling image: {}", imageName);
        try {
            docker.pullImageCmd(imageName)
                    .exec(new PullImageResultCallback() {
                        @Override
                        public void onNext(PullResponseItem item) {
                            if (item.getStatus() != null) {
                                log.debug("[pull {}] {}", imageName, item.getStatus());
                            }
                        }
                    })
                    .awaitCompletion(timeoutSeconds, TimeUnit.SECONDS);
            log.info("Image pulled successfully: {}", imageName);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Image pull interrupted: " + imageName, e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to pull image " + imageName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Returns {@code true} if the given image exists in the local Docker image cache.
     *
     * @param imageName the image name to check (with or without tag)
     */
    public boolean exists(String imageName) {
        try {
            docker.inspectImageCmd(imageName).exec();
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }

    /**
     * Ensures an image is available locally, pulling it if necessary.
     *
     * @param imageName      the image to ensure
     * @param timeoutSeconds pull timeout in seconds
     */
    public void ensurePresent(String imageName, int timeoutSeconds) {
        if (!exists(imageName)) {
            log.info("Image '{}' not found locally, pulling...", imageName);
            pull(imageName, timeoutSeconds);
        } else {
            log.debug("Image '{}' already present locally.", imageName);
        }
    }

    /**
     * Removes a Docker image from the local cache.
     *
     * @param imageName the image to remove
     * @param force     if {@code true}, removes even if containers are using it
     */
    public void remove(String imageName, boolean force) {
        try {
            docker.removeImageCmd(imageName).withForce(force).exec();
            log.info("Image removed: {}", imageName);
        } catch (NotFoundException e) {
            log.warn("Image not found when trying to remove: {}", imageName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to remove image " + imageName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Lists all Docker images currently in the local cache.
     *
     * @return list of all local images
     */
    public List<Image> listImages() {
        return docker.listImagesCmd().withShowAll(true).exec();
    }

    /**
     * Finds a local image by name.
     *
     * @param imageName the image name to search for
     * @return an {@link Optional} containing the image, or empty if not found
     */
    public Optional<Image> findImage(String imageName) {
        return listImages().stream()
                .filter(img -> img.getRepoTags() != null &&
                        List.of(img.getRepoTags()).contains(imageName))
                .findFirst();
    }
}