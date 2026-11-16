package com.docflow.ingestion.source;

import com.docflow.ingestion.client.github.GitHubClientProperties;
import com.docflow.ingestion.domain.exception.SourceException;
import com.docflow.ingestion.domain.model.RepositorySource;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitRepositorySourceProviderTest {

    @Mock
    private GitHubRepositoryUrlParser urlParser;

    @TempDir
    private Path tempDir;

    private GitRepositorySourceProvider sourceProvider;
    private GitHubClientProperties gitHubClientProperties;

    @BeforeEach
    void setUp() {
        gitHubClientProperties = new GitHubClientProperties();
        sourceProvider = new GitRepositorySourceProvider(urlParser, gitHubClientProperties);
    }
    @Test
    @DisplayName("Should delegate repository URL parsing to the parser")
    void shouldDelegateRepositoryUrlParsing() {
        String repositoryUrl = "https://github.com/owner/repository";
        RepositorySource expected = new RepositorySource(
                "owner",
                "repository",
                null,
                repositoryUrl,
                repositoryUrl
        );

        when(urlParser.validateAndExtract(repositoryUrl)).thenReturn(expected);
        RepositorySource result = sourceProvider.fromRepositoryUrl(repositoryUrl);

        assertThat(result).isSameAs(expected);
        verify(urlParser).validateAndExtract(repositoryUrl);
    }


    @Test
    @DisplayName("Should clone the repository into the workspace")
    void shouldCloneRepositoryIntoWorkspace() throws IOException, GitAPIException {
        Path sourcePath = createSourceRepository();

        Path workspacePath = tempDir.resolve("workspace");
        RepositorySource sourceInfo = new RepositorySource(
                "owner",
                "repository",
                null,
                sourcePath.toUri().toString(),
                "https://github.com/owner/repository"
        );

        sourceProvider.download(sourceInfo, workspacePath);

        assertThat(Files.isDirectory(workspacePath)).isTrue();
        assertThat(Files.readString(workspacePath.resolve("README.md")))
                .isEqualTo("repository content");
    }

    @Test
    @DisplayName("Should clone the repository when a GitHub token is configured")
    void shouldCloneRepositoryWhenGitHubTokenIsConfigured() throws IOException, GitAPIException {
        gitHubClientProperties.setToken("test-token");
        Path sourcePath = createSourceRepository();

        Path workspacePath = tempDir.resolve("workspace-with-token");
        RepositorySource sourceInfo = new RepositorySource(
                "owner",
                "repository",
                null,
                sourcePath.toUri().toString(),
                "https://github.com/owner/repository"
        );

        sourceProvider.download(sourceInfo, workspacePath);

        assertThat(Files.isDirectory(workspacePath)).isTrue();
        assertThat(Files.readString(workspacePath.resolve("README.md")))
                .isEqualTo("repository content");
    }

    @Test
    @DisplayName("Should reject an empty repository")
    void shouldRejectEmptyRepository() throws IOException, GitAPIException {
        Path sourcePath = Files.createTempDirectory(tempDir, "empty-source-");

        try (Git ignored = Git.init().setDirectory(sourcePath.toFile()).call()) {
            // Repositório sem nenhum commit.
        }

        String repositoryUrl = "https://github.com/owner/empty-repository";
        RepositorySource sourceInfo = new RepositorySource(
                "owner",
                "empty-repository",
                null,
                sourcePath.toUri().toString(),
                repositoryUrl
        );

        assertThatThrownBy(() -> sourceProvider.download(sourceInfo, tempDir.resolve("workspace")))
                .isInstanceOf(SourceException.class)
                .hasMessage("Repository 'owner/empty-repository' is empty: no commits found.");
    }

    @Test
    @DisplayName("Should wrap clone failures in a SourceException")
    void shouldWrapCloneFailureInSourceException() {
        Path missingRepositoryPath = tempDir.resolve("missing-repository");
        String repositoryUrl = "https://github.com/owner/repository";
        RepositorySource sourceInfo = new RepositorySource(
                "owner",
                "repository",
                null,
                missingRepositoryPath.toUri().toString(),
                repositoryUrl
        );

        assertThatThrownBy(() -> sourceProvider.download(sourceInfo, tempDir.resolve("workspace")))
                .isInstanceOf(SourceException.class)
                .hasMessage("Failed to clone repository 'owner/repository'.")
                .hasCauseInstanceOf(GitAPIException.class);
    }

    private Path createSourceRepository() throws IOException, GitAPIException {
        Path sourcePath = Files.createTempDirectory(tempDir, "source-");
        Files.writeString(sourcePath.resolve("README.md"), "repository content");

        try (Git git = Git.init().setDirectory(sourcePath.toFile()).call()) {
            git.add().addFilepattern("README.md").call();
            git.commit()
                    .setMessage("Initial commit")
                    .setAuthor("Test User", "test@example.com")
                    .call();
        }

        return sourcePath;
    }
}