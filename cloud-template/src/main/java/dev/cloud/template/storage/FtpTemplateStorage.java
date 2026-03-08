package dev.cloud.template.storage;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Template storage backend that reads and writes template files from an FTP server.
 * Downloads templates to a local cache directory before use.
 */
public class FtpTemplateStorage {

    private static final Logger log = LoggerFactory.getLogger(FtpTemplateStorage.class);

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String remoteRoot;
    private final Path localCache;

    /**
     * @param host        the FTP server hostname
     * @param port        the FTP server port (usually 21)
     * @param username    FTP username
     * @param password    FTP password
     * @param remoteRoot  the remote base path where templates are stored
     * @param localCache  local directory to cache downloaded files
     */
    public FtpTemplateStorage(String host, int port, String username,
                              String password, String remoteRoot, Path localCache) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.remoteRoot = remoteRoot;
        this.localCache = localCache;
    }

    /**
     * Downloads a template from the FTP server into the local cache directory.
     *
     * @param templateName the name of the template to download
     * @throws IOException if the FTP connection or transfer fails
     */
    public void download(String templateName) throws IOException {
        FTPClient ftp = connect();
        try {
            String remotePath = remoteRoot + "/" + templateName;
            Path localDir = localCache.resolve(templateName);
            Files.createDirectories(localDir);

            downloadDirectory(ftp, remotePath, localDir);
            log.info("Template '{}' downloaded from FTP to {}", templateName, localDir);
        } finally {
            disconnect(ftp);
        }
    }

    /**
     * Uploads a template directory to the FTP server.
     *
     * @param templateName the name of the template to upload
     * @param sourceDir    the local directory containing the template files
     * @throws IOException if the FTP connection or transfer fails
     */
    public void upload(String templateName, Path sourceDir) throws IOException {
        FTPClient ftp = connect();
        try {
            String remotePath = remoteRoot + "/" + templateName;
            uploadDirectory(ftp, sourceDir, remotePath);
            log.info("Template '{}' uploaded to FTP at {}", templateName, remotePath);
        } finally {
            disconnect(ftp);
        }
    }

    private FTPClient connect() throws IOException {
        FTPClient ftp = new FTPClient();
        ftp.connect(host, port);
        ftp.login(username, password);
        ftp.setFileType(FTP.BINARY_FILE_TYPE);
        ftp.enterLocalPassiveMode();
        return ftp;
    }

    private void disconnect(FTPClient ftp) {
        try {
            if (ftp.isConnected()) {
                ftp.logout();
                ftp.disconnect();
            }
        } catch (IOException e) {
            log.warn("Error disconnecting from FTP: {}", e.getMessage());
        }
    }

    private void downloadDirectory(FTPClient ftp, String remotePath, Path localDir) throws IOException {
        FTPFile[] files = ftp.listFiles(remotePath);
        if (files == null) return;

        for (FTPFile file : files) {
            String remoteFilePath = remotePath + "/" + file.getName();
            Path localFilePath = localDir.resolve(file.getName());

            if (file.isDirectory()) {
                Files.createDirectories(localFilePath);
                downloadDirectory(ftp, remoteFilePath, localFilePath);
            } else {
                try (OutputStream out = Files.newOutputStream(localFilePath)) {
                    ftp.retrieveFile(remoteFilePath, out);
                }
            }
        }
    }

    private void uploadDirectory(FTPClient ftp, Path localDir, String remotePath) throws IOException {
        ftp.makeDirectory(remotePath);
        try (var stream = Files.list(localDir)) {
            for (Path path : stream.toList()) {
                String remoteFilePath = remotePath + "/" + path.getFileName();
                if (Files.isDirectory(path)) {
                    uploadDirectory(ftp, path, remoteFilePath);
                } else {
                    try (InputStream in = Files.newInputStream(path)) {
                        ftp.storeFile(remoteFilePath, in);
                    }
                }
            }
        }
    }
}