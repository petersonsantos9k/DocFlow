package com.docflow.ingestion.orchestrator;


import com.docflow.ingestion.client.RepositoryMetadataClient;
import com.docflow.ingestion.config.IngestionProperties;
import com.docflow.ingestion.domain.exception.IngestionException;
import com.docflow.ingestion.domain.exception.SourceException;
import com.docflow.ingestion.domain.model.IngestionContext;
import com.docflow.ingestion.domain.model.RepositorySource;
import com.docflow.ingestion.domain.model.WorkspaceInfo;
import com.docflow.ingestion.source.SourceProvider;
import com.docflow.ingestion.workspace.WorkspaceProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.stream.Stream;

@Slf4j
@Service
public class IngestionOrchestrator {

    private final SourceProvider sourceProvider;
    private final WorkspaceProvider workspaceProvider;
    private final RepositoryMetadataClient repositoryClient;
    private final DataSize maxRepositorySize;


    public IngestionOrchestrator(
            @Qualifier("jGitSourceProvider") SourceProvider sourceProvider,
            @Qualifier("temporaryWorkspaceProvider") WorkspaceProvider workspaceProvider,
            @Qualifier("gitHubRepositoryClient") RepositoryMetadataClient repositoryClient,
            IngestionProperties ingestionProperties
            ){
        this.sourceProvider = sourceProvider;
        this.workspaceProvider = workspaceProvider;
        this.repositoryClient = repositoryClient;
        this.maxRepositorySize = ingestionProperties.getMaxRepositorySize();
    }

    public IngestionSession ingest(String repositoryUrl){

        RepositorySource sourceInfo = sourceProvider.fromRepositoryUrl(repositoryUrl);
        isRepoSizeAcceptable(sourceInfo.owner(), sourceInfo.repo());
        WorkspaceInfo workspaceInfo = workspaceProvider.createWorkspace();

        boolean succeeded = false;

        try{
            sourceProvider.download(sourceInfo, workspaceInfo.path());
            validateRepositorySize(
                    workspaceInfo.path(),
                    maxRepositorySize,
                    sourceInfo.owner(),
                    sourceInfo.repo());

            Path sourceRoot = resolveSourceRoot(
                    workspaceInfo.path(),
                    sourceInfo.subPath(),
                    sourceInfo.owner(),
                    sourceInfo.repo());

            IngestionContext context = new IngestionContext(
                    workspaceInfo.id(),
                    sourceInfo.owner(),
                    sourceInfo.repo(),
                    sourceInfo.subPath(),
                    sourceInfo.cloneUrl(),
                    workspaceInfo.path(),
                    sourceRoot
            );

            succeeded = true;
            return new IngestionSession(context, workspaceInfo, workspaceProvider);
        } finally {
            if (!succeeded) {
                deleteWorkspaceAfterFailure(workspaceInfo);
            }
        }

    }

    private void deleteWorkspaceAfterFailure(WorkspaceInfo workspaceInfo) {
        try{
            workspaceProvider.deleteWorkspace(workspaceInfo);
        } catch (RuntimeException cleanupErr){
            log.warn("Failed to delete workspace after ingestion failure: {}",
                    workspaceInfo.path(), cleanupErr);
        }
    }

    private void validateRepositorySize(
            Path workspacePath,
            DataSize maxSize,
            String owner,
            String repo) {
        long limit = maxSize.toBytes();
        long total = 0;

        try (Stream<Path> paths = Files.walk(workspacePath)) {
            var iterator = paths.iterator();
            while (iterator.hasNext()) {
                Path path = iterator.next();
                if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
                    continue;
                }

                total += Files.size(path);
                if (total > limit) {
                    throw new SourceException(
                            "Cloned repository '" + owner + "/" + repo
                                    + "' exceeds the maximum allowed size of "
                                    + formatSize(maxSize) + ".");
                }
            }
        } catch (IOException err) {
            throw new SourceException(
                    "Failed to measure the size of the cloned repository at '"
                            + workspacePath + "'.", err);
        }
    }

    private String formatSize(DataSize size) {
        return size.toBytes() < 1024 * 1024
                ? size.toBytes() + " bytes"
                : size.toMegabytes() + " MB";
    }

    private String formatSize(long bytes) {
        return formatSize(DataSize.ofBytes(bytes));
    }

    private Path resolveSourceRoot(
            Path workspacePath,
            String subPath,
            String owner,
            String repo) {
        Path realWorkspace = toRealPath(
                workspacePath,
                "Failed to resolve workspace directory at '" + workspacePath + "'.");

        Path candidate = subPath == null || subPath.isBlank()
                ? realWorkspace
                : realWorkspace.resolve(subPath).normalize();

        if (!candidate.startsWith(realWorkspace)) {
            throw new SourceException(
                    "Repository subpath '" + subPath
                            + "' resolves outside the repository workspace.");
        }

        Path sourceRoot = toRealPath(
                candidate,
                "Repository subpath '" + subPath + "' does not exist in '"
                        + owner + "/" + repo + "'.");

        if (!sourceRoot.startsWith(realWorkspace)) {
            throw new SourceException(
                    "Repository subpath '" + subPath
                            + "' resolves outside the repository workspace.");
        }

        if (!Files.isDirectory(sourceRoot)) {
            throw new SourceException(
                    "Repository subpath '" + subPath + "' does not exist in '"
                            + owner + "/" + repo + "'.");
        }

        return sourceRoot;
    }

    private Path toRealPath(Path path, String errorMessage) {
        try {
            return path.toRealPath();
        } catch (IOException err) {
            throw new SourceException(errorMessage, err);
        }
    }

    private void isRepoSizeAcceptable(String owner, String repo) {

        var response = repositoryClient.getRepository(owner, repo).orElseThrow(
                () -> new IngestionException(
                        "Repository '" + owner + "/" + repo
                                + "' was not found or is not accessible with the configured credentials.")
        );

        long repoSizeInBytes = response.sizeInKb() * 1024L;
        if(repoSizeInBytes > maxRepositorySize.toBytes()){
            throw new IngestionException(
                    "Repository '" + owner + "/" + repo + "' is "
                            + formatSize(repoSizeInBytes)
                            + " and exceeds the configured limit of "
                            + formatSize(maxRepositorySize) + ".");
        }
    }


}
