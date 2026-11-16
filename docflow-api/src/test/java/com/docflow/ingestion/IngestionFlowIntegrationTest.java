package com.docflow.ingestion;

import com.docflow.ingestion.client.RepositoryMetadataClient;
import com.docflow.ingestion.client.github.GitHubClientProperties;
import com.docflow.ingestion.config.IngestionProperties;
import com.docflow.ingestion.domain.exception.SourceException;
import com.docflow.ingestion.domain.model.GitHubRepositoryInfo;
import com.docflow.ingestion.domain.model.IngestionContext;
import com.docflow.ingestion.domain.model.RepositorySource;
import com.docflow.ingestion.orchestrator.IngestionService;
import com.docflow.ingestion.orchestrator.IngestionSession;
import com.docflow.ingestion.source.GitHubRepositoryUrlParser;
import com.docflow.ingestion.source.GitRepositorySourceProvider;
import com.docflow.ingestion.source.SourceProvider;
import com.docflow.ingestion.workspace.TemporaryWorkspaceProvider;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.util.unit.DataSize;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IngestionFlowIntegrationTest {

    private static final String URL = "https://github.com/owner/repository";

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Should ingest a local repository and clean up the workspace")
    void shouldIngestLocalRepositoryAndCleanUpWorkspace() throws IOException, GitAPIException {
        Path repository = createRepository();
        IngestionProperties properties = ingestionProperties();
        IngestionService service = ingestionService(repository, properties);

        Path workspace;
        try (IngestionSession session = service.ingest(URL)) {
            IngestionContext context = session.context();
            workspace = session.workspacePath();

            assertThat(context.owner()).isEqualTo("owner");
            assertThat(context.repo()).isEqualTo("repository");
            assertThat(context.subPath()).isEqualTo("module/api");
            assertThat(workspace.startsWith(
                    properties.getWorkspaceRoot().toAbsolutePath().normalize())).isTrue();
            assertThat(session.sourceRoot())
                    .isEqualTo(workspace.resolve("module/api").toRealPath());
            assertThat(Files.exists(session.sourceRoot().resolve("DemoController.java"))).isTrue();
        }

        assertThat(Files.exists(workspace)).isFalse();
    }

    @Test
    @DisplayName("Should clean up the workspace when the clone fails")
    void shouldCleanUpWorkspaceWhenCloneFails() throws IOException {
        IngestionProperties properties = ingestionProperties();
        IngestionService service = ingestionService(
                tempDir.resolve("missing-repository"),
                properties);

        assertThatThrownBy(() -> service.ingest(URL))
                .isInstanceOf(SourceException.class)
                .hasMessage("Failed to clone repository 'owner/repository'.");

        try (Stream<Path> entries = Files.list(properties.getWorkspaceRoot())) {
            assertThat(entries).isEmpty();
        }
    }

    private IngestionService ingestionService(
            Path repositoryPath,
            IngestionProperties properties) {
        GitHubClientProperties gitHubProperties = new GitHubClientProperties();
        GitRepositorySourceProvider gitProvider = new GitRepositorySourceProvider(
                new GitHubRepositoryUrlParser(),
                gitHubProperties);

        RepositorySource source = new RepositorySource(
                "owner",
                "repository",
                "module/api",
                repositoryPath.toUri().toString(),
                URL);

        SourceProvider sourceProvider = new LocalSourceProvider(gitProvider, source);

        RepositoryMetadataClient metadataClient = (owner, repo) ->
                Optional.of(new GitHubRepositoryInfo(false, 0, "Java"));

        return new IngestionService(
                sourceProvider,
                new TemporaryWorkspaceProvider(properties),
                metadataClient,
                properties);
    }

    private IngestionProperties ingestionProperties() {
        IngestionProperties properties = new IngestionProperties();
        properties.setWorkspaceRoot(tempDir.resolve("jobs"));
        properties.setMaxRepositorySize(DataSize.ofMegabytes(10));
        return properties;
    }

    private Path createRepository() throws IOException, GitAPIException {
        Path repository = tempDir.resolve("repository");
        Files.createDirectories(repository.resolve("module/api"));
        Files.writeString(
                repository.resolve("module/api/DemoController.java"),
                "class DemoController {}");

        try (Git git = Git.init().setDirectory(repository.toFile()).call()) {
            git.add().addFilepattern(".").call();
            git.commit()
                    .setMessage("Initial commit")
                    .setAuthor("Test User", "test@example.com")
                    .call();
        }

        return repository;
    }

    private static final class LocalSourceProvider implements SourceProvider {

        private final GitRepositorySourceProvider delegate;
        private final RepositorySource source;

        private LocalSourceProvider(
                GitRepositorySourceProvider delegate,
                RepositorySource source) {
            this.delegate = delegate;
            this.source = source;
        }

        @Override
        public RepositorySource fromRepositoryUrl(String repositoryUrl) {
            return source;
        }

        @Override
        public void download(RepositorySource repositorySource, Path workspacePath) {
            delegate.download(repositorySource, workspacePath);
        }
    }
}
