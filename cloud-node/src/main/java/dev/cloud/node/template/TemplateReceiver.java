package dev.cloud.node.template;

import dev.cloud.networking.template.TemplateRpcClient;
import dev.cloud.template.storage.LocalTemplateStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Downloads templates from the master via gRPC and stores them in the local cache.
 * Called when a node needs a template that is not yet cached or is outdated.
 */
public class TemplateReceiver {

    private static final Logger log = LoggerFactory.getLogger(TemplateReceiver.class);

    private final TemplateRpcClient rpcClient;
    private final LocalTemplateStorage localStorage;

    public TemplateReceiver(TemplateRpcClient rpcClient, LocalTemplateStorage localStorage) {
        this.rpcClient     = rpcClient;
        this.localStorage  = localStorage;
    }

    /**
     * Downloads the given template from the master if the local cache is missing or outdated.
     *
     * @param templateName the name of the template to receive
     * @throws IOException if the download or local storage write fails
     */
    public void receive(String templateName) throws IOException {
        Path localPath = localStorage.getTemplatePath(templateName);
        boolean needsDownload = rpcClient.needsSync(templateName, localPath);

        if (needsDownload) {
            log.info("Downloading template '{}' from master...", templateName);
            rpcClient.downloadTemplate(templateName);
            log.info("Template '{}' downloaded and cached.", templateName);
        } else {
            log.debug("Template '{}' is up to date.", templateName);
        }
    }
}