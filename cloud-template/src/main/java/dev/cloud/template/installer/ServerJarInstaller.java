package dev.cloud.template.installer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/**
 * Abstract base for server JAR installers.
 * Provides HTTP download utilities and defines the install contract.
 * Subclasses implement platform-specific download logic (Paper, Velocity, etc.).
 */
public abstract class ServerJarInstaller {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected final HttpClient http;

    protected ServerJarInstaller() {
        this.http = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    /**
     * Downloads and installs the server JAR into the given directory.
     *
     * @param version   the server version to install (e.g. {@code "1.21.1"})
     * @param targetDir the directory to install the JAR into
     * @throws IOException          if the download or file write fails
     * @throws InterruptedException if the HTTP request is interrupted
     */
    public abstract void install(String version, Path targetDir) throws IOException, InterruptedException;

    /**
     * Returns the name of the JAR file as it should be saved in the target directory.
     * (e.g. {@code "server.jar"}, {@code "velocity.jar"})
     */
    public abstract String getJarFileName();

    /**
     * Downloads a file from the given URL to the target path.
     *
     * @param url    the URL to download from
     * @param target the local path to save the file to
     * @throws IOException          if the HTTP response is not 200 or writing fails
     * @throws InterruptedException if the request is interrupted
     */
    protected void download(String url, Path target) throws IOException, InterruptedException {
        log.info("Downloading: {}", url);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(5))
                .GET()
                .build();

        HttpResponse<InputStream> response = http.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() != 200) {
            throw new IOException("Download failed with HTTP " + response.statusCode() + " for: " + url);
        }

        Files.createDirectories(target.getParent());
        try (InputStream in = response.body()) {
            Files.copy(in, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        log.info("Downloaded {} ({} KB)", target.getFileName(), Files.size(target) / 1024);
    }

    /**
     * Performs an HTTP GET and returns the response body as a string.
     * Used to query version APIs.
     *
     * @param url the URL to fetch
     * @return the response body
     * @throws IOException          if the request fails
     * @throws InterruptedException if the request is interrupted
     */
    protected String getJson(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("API request failed with HTTP " + response.statusCode() + " for: " + url);
        }

        return response.body();
    }
}