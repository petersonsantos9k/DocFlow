package com.docflow.ingestion.orchestrator;

import com.docflow.ingestion.client.RepositoryMetadataClient;
import com.docflow.ingestion.config.IngestionProperties;
import com.docflow.ingestion.domain.exception.SourceException;
import com.docflow.ingestion.domain.exception.WorkspaceException;
import com.docflow.ingestion.domain.model.IngestionContext;
import com.docflow.ingestion.domain.model.GitHubRepositoryInfo;
import com.docflow.ingestion.domain.model.RepositorySource;
import com.docflow.ingestion.domain.model.WorkspaceInfo;
import com.docflow.ingestion.source.SourceProvider;
import com.docflow.ingestion.workspace.WorkspaceProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.unit.DataSize;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngestionServiceTest {

    private static final String URL =
            "https://github.com/petersonsantos9k/picpay-desafio-backend";

    @Mock
    private SourceProvider sourceProvider;
    @Mock
    private WorkspaceProvider workspaceProvider;
    @Mock
    private RepositoryMetadataClient repositoryClient;
    @TempDir
    Path tempDir;

    private IngestionService ingestionService;
    private IngestionProperties ingestionProperties;
    private RepositorySource sourceInfo;
    private WorkspaceInfo workspaceInfo;

    @BeforeEach
    void setUp() throws IOException {
        ingestionProperties = new IngestionProperties();
        ingestionProperties.setMaxRepositorySize(DataSize.ofMegabytes(30));

        ingestionService = new IngestionService(
                sourceProvider,
                workspaceProvider,
                repositoryClient,
                ingestionProperties
        );
        sourceInfo = new RepositorySource(
                "PetersonSantos9K",
                "repository-teste-docflow",
                null,
                "https://github.com/PetersonSantos9K/repository-teste-docflow",
                URL
        );
        workspaceInfo = new WorkspaceInfo(
                UUID.randomUUID(),
                tempDir.resolve("workspace")
        );
        Files.createDirectories(workspaceInfo.path());
    }

    @Test
    @DisplayName("Should return an ingestion context when ingestion succeeds")
    void shouldReturnIngestionContextWhenIngestionSucceeds() throws IOException {

        stubSuccessfulStart();

        try (IngestionSession session = ingestionService.ingest(URL)) {
            IngestionContext context = session.context();

            assertThat(context).isNotNull();
            assertThat(context.id()).isEqualTo(workspaceInfo.id());
            assertThat(context.owner()).isEqualTo(sourceInfo.owner());
            assertThat(context.repo()).isEqualTo(sourceInfo.repo());
            assertThat(context.subPath()).isEqualTo(sourceInfo.subPath());
            assertThat(context.workspacePath()).isEqualTo(workspaceInfo.path());
            assertThat(context.sourceRoot())
                    .isEqualTo(workspaceInfo.path().toRealPath());
            assertThat(context.urlRepositoryCloned()).isEqualTo(sourceInfo.cloneUrl());
            assertThat(session.workspacePath()).isEqualTo(workspaceInfo.path());
            assertThat(session.sourceRoot())
                    .isEqualTo(workspaceInfo.path().toRealPath());

            verify(sourceProvider).download(sourceInfo, workspaceInfo.path());
        }

        verify(workspaceProvider).deleteWorkspace(workspaceInfo);
    }

    @Test
    @DisplayName("Should delete the workspace only once when the session is closed multiple times")
    void shouldDeleteWorkspaceOnlyOnce() {

        stubSuccessfulStart();

        try (IngestionSession session = ingestionService.ingest(URL)) {
            session.close();
        }

        verify(workspaceProvider, times(1)).deleteWorkspace(workspaceInfo);
    }

    @Test
    @DisplayName("Should delete the workspace when the download fails")
    void shouldDeleteWorkspaceWhenDownloadFails() {

        stubSuccessfulStart();
        doThrow(new SourceException("download failed"))
                .when(sourceProvider).download(sourceInfo, workspaceInfo.path());

        assertThatThrownBy(() -> ingestionService.ingest(URL))
                .isInstanceOf(SourceException.class)
                .hasMessage("download failed");

        verify(workspaceProvider).deleteWorkspace(workspaceInfo);
    }

    @Test
    @DisplayName("Should delete the workspace when an unexpected failure occurs")
    void shouldDeleteWorkspaceWhenUnexpectedFailureOccurs() {

        stubSuccessfulStart();
        doThrow(new IllegalStateException("unexpected failure"))
                .when(sourceProvider).download(sourceInfo, workspaceInfo.path());

        assertThatThrownBy(() -> ingestionService.ingest(URL))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("unexpected failure");

        verify(workspaceProvider).deleteWorkspace(workspaceInfo);
    }

    @Test
    @DisplayName("Should not mask the ingestion failure when workspace cleanup fails")
    void shouldNotMaskIngestionFailureWhenCleanupFails() {

        stubSuccessfulStart();
        doThrow(new SourceException("download failed"))
                .when(sourceProvider).download(sourceInfo, workspaceInfo.path());
        doThrow(new WorkspaceException("cleanup failed", new IOException("io failure")))
                .when(workspaceProvider).deleteWorkspace(any());

        assertThatThrownBy(() -> ingestionService.ingest(URL))
                .isInstanceOf(SourceException.class)
                .hasMessage("download failed");
    }

    @Test
    @DisplayName("Should resolve the source root from the repository subpath")
    void shouldResolveSourceRootFromSubPath() throws IOException {

        RepositorySource sourceInfoWithSubPath = sourceInfoWithSubPath("module/api");
        Path expectedSourceRoot = workspaceInfo.path().resolve("module/api");
        Files.createDirectories(expectedSourceRoot);

        stubSuccessfulStart(sourceInfoWithSubPath);

        try (IngestionSession session = ingestionService.ingest(URL)) {
            assertThat(session.sourceRoot())
                    .isEqualTo(expectedSourceRoot.toRealPath());
            assertThat(session.context().sourceRoot())
                    .isEqualTo(expectedSourceRoot.toRealPath());
        }

        verify(workspaceProvider).deleteWorkspace(workspaceInfo);
    }

    @Test
    @DisplayName("Should reject a cloned repository that exceeds the maximum size")
    void shouldRejectRepositoryExceedingMaximumSizeAfterClone() throws IOException {

        IngestionProperties limitedProperties = new IngestionProperties();
        limitedProperties.setMaxRepositorySize(DataSize.ofBytes(10));

        IngestionService limitedOrchestrator = new IngestionService(
                sourceProvider,
                workspaceProvider,
                repositoryClient,
                limitedProperties
        );
        stubSuccessfulStart();
        when(repositoryClient.getRepository(sourceInfo.owner(), sourceInfo.repo()))
                .thenReturn(Optional.of(new GitHubRepositoryInfo(false, 0, "Java")));
        Files.writeString(
                workspaceInfo.path().resolve("large.txt"),
                "more than ten bytes"
        );

        assertThatThrownBy(() -> limitedOrchestrator.ingest(URL))
                .isInstanceOf(SourceException.class)
                .hasMessage("Cloned repository 'PetersonSantos9K/repository-teste-docflow' "
                        + "exceeds the maximum allowed size of 10 bytes.");

        verify(workspaceProvider).deleteWorkspace(workspaceInfo);
    }

    @Test
    @DisplayName("Should reject a subpath that escapes the workspace")
    void shouldRejectSubPathOutsideWorkspace() {

        stubSuccessfulStart(sourceInfoWithSubPath("../outside"));

        assertThatThrownBy(() -> ingestionService.ingest(URL))
                .isInstanceOf(SourceException.class)
                .hasMessage("Repository subpath '../outside' "
                        + "resolves outside the repository workspace.");

        verify(workspaceProvider).deleteWorkspace(workspaceInfo);
    }

    @Test
    @DisplayName("Should reject a subpath that does not exist in the repository")
    void shouldRejectMissingSubPath() {

        stubSuccessfulStart(sourceInfoWithSubPath("missing-module"));

        assertThatThrownBy(() -> ingestionService.ingest(URL))
                .isInstanceOf(SourceException.class)
                .hasMessage("Repository subpath 'missing-module' does not exist in "
                        + "'PetersonSantos9K/repository-teste-docflow'.");

        verify(workspaceProvider).deleteWorkspace(workspaceInfo);
    }

    @Test
    @DisplayName("Should reject a subpath that escapes the workspace through a symbolic link")
    void shouldRejectSubPathThroughSymbolicLink() throws IOException {

        Path outside = tempDir.resolve("outside");
        Files.createDirectories(outside);
        Path link = workspaceInfo.path().resolve("linked-outside");

        try {
            Files.createSymbolicLink(link, outside);
        } catch (UnsupportedOperationException | IOException err) {
            assumeTrue(false, "Symbolic links are not supported in this environment");
        }

        stubSuccessfulStart(sourceInfoWithSubPath("linked-outside"));

        assertThatThrownBy(() -> ingestionService.ingest(URL))
                .isInstanceOf(SourceException.class)
                .hasMessage("Repository subpath 'linked-outside' "
                        + "resolves outside the repository workspace.");

        verify(workspaceProvider).deleteWorkspace(workspaceInfo);
    }

    private void stubSuccessfulStart() {
        stubSuccessfulStart(sourceInfo);
    }

    private void stubSuccessfulStart(RepositorySource sourceInfo) {
        when(sourceProvider.fromRepositoryUrl(URL)).thenReturn(sourceInfo);
        when(repositoryClient.getRepository(sourceInfo.owner(), sourceInfo.repo()))
                .thenReturn(Optional.of(new GitHubRepositoryInfo(false, 30, "Java")));
        when(workspaceProvider.createWorkspace()).thenReturn(workspaceInfo);
    }

    private RepositorySource sourceInfoWithSubPath(String subPath) {
        return new RepositorySource(
                sourceInfo.owner(),
                sourceInfo.repo(),
                subPath,
                sourceInfo.cloneUrl(),
                sourceInfo.urlRepository()
        );
    }
}
