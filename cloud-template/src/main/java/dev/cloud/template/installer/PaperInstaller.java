package dev.cloud.template.installer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Downloads the latest Paper server JAR for a given Minecraft version
 * from the PaperMC API (https://api.papermc.io).
 */
public class PaperInstaller extends ServerJarInstaller {

    private static final String API_BASE = "https://api.papermc.io/v2/projects/paper";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void install(String version, Path targetDir) throws IOException, InterruptedException {
        int latestBuild = fetchLatestBuild(version);
        String downloadUrl = buildDownloadUrl(version, latestBuild);
        Path target = targetDir.resolve(getJarFileName());
        download(downloadUrl, target);
        log.info("Paper {} build {} installed at {}", version, latestBuild, target);
    }

    @Override
    public String getJarFileName() {
        return "server.jar";
    }

    /**
     * Fetches the latest Paper build number for the given Minecraft version.
     *
     * @param version the Minecraft version (e.g. {@code "1.21.1"})
     * @return the latest build number
     */
    private int fetchLatestBuild(String version) throws IOException, InterruptedException {
        String url = API_BASE + "/versions/" + version;
        String json = getJson(url);
        JsonNode node = MAPPER.readTree(json);
        JsonNode builds = node.get("builds");
        return builds.get(builds.size() - 1).asInt();
    }

    /**
     * Constructs the download URL for a specific Paper version and build.
     */
    private String buildDownloadUrl(String version, int build) {
        String jarName = "paper-" + version + "-" + build + ".jar";
        return API_BASE + "/versions/" + version + "/builds/" + build + "/downloads/" + jarName;
    }
}