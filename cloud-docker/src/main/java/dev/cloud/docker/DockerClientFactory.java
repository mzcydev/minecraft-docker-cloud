package dev.cloud.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Factory for creating and configuring a {@link DockerClient} instance.
 * Supports both Unix socket (default) and TCP connections to the Docker daemon.
 */
public class DockerClientFactory {

    private static final Logger log = LoggerFactory.getLogger(DockerClientFactory.class);

    private static final String DEFAULT_UNIX_SOCKET = "unix:///var/run/docker.sock";

    private final String dockerHost;

    /**
     * Creates a factory that connects via the default Unix socket.
     */
    public DockerClientFactory() {
        this(DEFAULT_UNIX_SOCKET);
    }

    /**
     * Creates a factory that connects to the given Docker host.
     *
     * @param dockerHost the Docker daemon URI (e.g. {@code "unix:///var/run/docker.sock"}
     *                   or {@code "tcp://192.168.1.10:2375"})
     */
    public DockerClientFactory(String dockerHost) {
        this.dockerHost = dockerHost;
    }

    /**
     * Builds and returns a configured {@link DockerClient}.
     * The client is connected lazily — no actual connection is made until the first API call.
     *
     * @return a ready-to-use {@link DockerClient}
     */
    public DockerClient create() {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(10))
                .responseTimeout(Duration.ofSeconds(30))
                .build();

        log.info("Docker client created for host: {}", dockerHost);
        return DockerClientImpl.getInstance(config, httpClient);
    }

    /**
     * Verifies the connection to the Docker daemon by pinging it.
     *
     * @param client the client to test
     * @return {@code true} if the daemon responded, {@code false} otherwise
     */
    public boolean testConnection(DockerClient client) {
        try {
            client.pingCmd().exec();
            log.info("Docker daemon connection successful.");
            return true;
        } catch (Exception e) {
            log.error("Docker daemon not reachable at '{}': {}", dockerHost, e.getMessage());
            return false;
        }
    }
}