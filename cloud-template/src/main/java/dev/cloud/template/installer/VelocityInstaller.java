package dev.cloud.template.installer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Downloads the latest Velocity proxy JAR for a given version
 * from the PaperMC API (https://api.papermc.io).
 */
public class VelocityInstaller extends ServerJarInstaller {

    private static final String API_BASE = "https://api.papermc.io/v2/projects/velocity";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void install(String version, Path targetDir) throws IOException, InterruptedException {
        int latestBuild = fetchLatestBuild(version);
        String downloadUrl = buildDownloadUrl(version, latestBuild);
        Path target = targetDir.resolve(getJarFileName());
        download(downloadUrl, target);
        log.info("Velocity {} build {} installed at {}", version, latestBuild, target);
    }

    @Override
    public String getJarFileName() {
        return "velocity.jar";
    }

    private int fetchLatestBuild(String version) throws IOException, InterruptedException {
        String url = API_BASE + "/versions/" + version;
        String json = getJson(url);
        JsonNode node = MAPPER.readTree(json);
        JsonNode builds = node.get("builds");
        return builds.get(builds.size() - 1).asInt();
    }

    private String buildDownloadUrl(String version, int build) {
        String jarName = "velocity-" + version + "-" + build + ".jar";
        return API_BASE + "/versions/" + version + "/builds/" + build + "/downloads/" + jarName;
    }
}