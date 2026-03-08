package dev.cloud.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.function.Consumer;

/**
 * Streams log output from a running Docker container to a consumer callback.
 * Useful for forwarding server logs to the cloud console or storing them.
 */
public class ContainerLogStreamer {

    private static final Logger log = LoggerFactory.getLogger(ContainerLogStreamer.class);

    private final DockerClient docker;

    public ContainerLogStreamer(DockerClient docker) {
        this.docker = docker;
    }

    /**
     * Starts streaming logs from the given container.
     * Each line is passed to the provided consumer as a plain string.
     * The stream runs in a background thread until the container stops or {@link Closeable#close()} is called.
     *
     * @param containerId  the container whose logs to stream
     * @param lineConsumer a callback that receives each log line
     * @return a {@link Closeable} that stops the log stream when closed
     */
    public Closeable stream(String containerId, Consumer<String> lineConsumer) {
        log.debug("Starting log stream for container {}", containerId);

        ResultCallback.Adapter<Frame> callback = new ResultCallback.Adapter<>() {
            @Override
            public void onNext(Frame frame) {
                if (frame != null && frame.getPayload() != null) {
                    String line = new String(frame.getPayload()).stripTrailing();
                    if (!line.isBlank()) {
                        lineConsumer.accept(line);
                    }
                }
            }

            @Override
            public void onError(Throwable throwable) {
                log.warn("Log stream error for container {}: {}", containerId, throwable.getMessage());
            }

            @Override
            public void onComplete() {
                log.debug("Log stream completed for container {}", containerId);
            }
        };

        docker.logContainerCmd(containerId)
                .withStdOut(true)
                .withStdErr(true)
                .withFollowStream(true)
                .withTailAll()
                .exec(callback);

        return callback;
    }
}