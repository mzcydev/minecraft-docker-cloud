package dev.cloud.networking.template;

import dev.cloud.api.template.TemplateManager;
import dev.cloud.api.template.TemplateVersion;
import dev.cloud.proto.common.Empty;
import dev.cloud.proto.template.*;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

/**
 * Master-side gRPC implementation of {@link TemplateServiceGrpc.TemplateServiceImplBase}.
 * Serves template metadata and streams template files to requesting nodes.
 */
public class TemplateRpcService extends TemplateServiceGrpc.TemplateServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(TemplateRpcService.class);

    /** Size of each streamed file chunk in bytes (512 KB). */
    private static final int CHUNK_SIZE = 512 * 1024;

    private final TemplateManager templateManager;
    /** Root directory where templates are stored on the master's filesystem. */
    private final Path templatesRoot;

    public TemplateRpcService(TemplateManager templateManager, Path templatesRoot) {
        this.templateManager = templateManager;
        this.templatesRoot = templatesRoot;
    }

    @Override
    public void listTemplates(Empty request, StreamObserver<ListTemplatesResponse> observer) {
        ListTemplatesResponse response = ListTemplatesResponse.newBuilder()
                .addAllTemplates(templateManager.getAllTemplates().stream()
                        .map(t -> ProtoTemplate.newBuilder()
                                .setName(t.getName())
                                .setStorageType(t.getStorage().name())
                                .setPath(t.getPath())
                                .build())
                        .collect(Collectors.toList()))
                .build();
        observer.onNext(response);
        observer.onCompleted();
    }

    @Override
    public void getTemplate(GetTemplateRequest request, StreamObserver<GetTemplateResponse> observer) {
        templateManager.getTemplate(request.getTemplateName()).ifPresentOrElse(
                t -> {
                    TemplateVersion version = templateManager.getVersion(t.getName()).orElse(null);
                    ProtoTemplate.Builder builder = ProtoTemplate.newBuilder()
                            .setName(t.getName())
                            .setStorageType(t.getStorage().name())
                            .setPath(t.getPath());
                    if (version != null) {
                        builder.setChecksum(version.checksum())
                                .setSizeBytes(version.sizeBytes());
                    }
                    observer.onNext(GetTemplateResponse.newBuilder().setTemplate(builder.build()).build());
                },
                () -> observer.onNext(GetTemplateResponse.getDefaultInstance())
        );
        observer.onCompleted();
    }

    @Override
    public void checkSync(TemplateSyncRequest request, StreamObserver<TemplateSyncResponse> observer) {
        String name = request.getTemplateName();
        String localChecksum = request.getLocalChecksum();

        TemplateSyncResponse.Builder response = TemplateSyncResponse.newBuilder();

        templateManager.getVersion(name).ifPresentOrElse(
                version -> {
                    boolean needsUpdate = version.isNewerThan(localChecksum);
                    response.setNeedsUpdate(needsUpdate)
                            .setChecksum(version.checksum())
                            .setSizeBytes(version.sizeBytes());
                },
                () -> response.setNeedsUpdate(true)
        );

        observer.onNext(response.build());
        observer.onCompleted();
    }

    /**
     * Streams the files of a template to the requesting node as a sequence of {@link TemplateChunk} messages.
     * Each file is split into {@value #CHUNK_SIZE}-byte chunks.
     */
    @Override
    public void downloadTemplate(GetTemplateRequest request, StreamObserver<TemplateChunk> observer) {
        String templateName = request.getTemplateName();
        Path templateDir = templatesRoot.resolve(templateName);

        if (!Files.exists(templateDir)) {
            observer.onError(new IllegalArgumentException("Template not found: " + templateName));
            return;
        }

        try {
            var files = Files.walk(templateDir)
                    .filter(Files::isRegularFile)
                    .toList();

            for (Path file : files) {
                String relativePath = templateDir.relativize(file).toString().replace("\\", "/");
                streamFile(observer, templateName, relativePath, file);
            }

            observer.onCompleted();
            log.info("Finished streaming template '{}' ({} files)", templateName, files.size());

        } catch (IOException e) {
            log.error("Error streaming template '{}'", templateName, e);
            observer.onError(e);
        }
    }

    private void streamFile(StreamObserver<TemplateChunk> observer,
                            String templateName, String relativePath, Path file) throws IOException {
        long fileSize = Files.size(file);
        int totalChunks = (int) Math.ceil((double) fileSize / CHUNK_SIZE);
        if (totalChunks == 0) totalChunks = 1;

        try (InputStream in = Files.newInputStream(file)) {
            byte[] buffer = new byte[CHUNK_SIZE];
            int chunkIndex = 0;
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                boolean isLast = (chunkIndex == totalChunks - 1);
                observer.onNext(TemplateChunk.newBuilder()
                        .setTemplateName(templateName)
                        .setRelativePath(relativePath)
                        .setChunkIndex(chunkIndex)
                        .setTotalChunks(totalChunks)
                        .setData(com.google.protobuf.ByteString.copyFrom(buffer, 0, bytesRead))
                        .setLastChunk(isLast)
                        .build());
                chunkIndex++;
            }
        }
    }
}