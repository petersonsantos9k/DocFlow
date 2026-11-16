package com.docflow.ingestion.source;

import com.docflow.ingestion.domain.exception.SourceException;
import com.docflow.ingestion.domain.model.RepositorySource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class GitHubRepositoryUrlParserTest {

    private GitHubRepositoryUrlParser urlParser;

    @BeforeEach
    void setup(){
        urlParser = new GitHubRepositoryUrlParser();
    }

    @Test
    @DisplayName("Should accept a valid repository URL")
    void shouldAcceptValidUrl() {
        String prefix = "https://github.com";
        String owner = "PetersonSantos9K";
        String repo = "repository-teste-docflow";
        String url = prefix + "/" + owner + "/" + repo;

        var obj = urlParser.validateAndExtract(url);
        assertThat(obj)
                .isNotNull()
                .isInstanceOf(RepositorySource.class);

        assertThat(obj.owner()).isEqualTo(owner);
        assertThat(obj.repo()).isEqualTo(repo);
        assertThat(obj.subPath()).isNullOrEmpty();
        assertThat(obj.cloneUrl()).isEqualTo(url);
        assertThat(obj.urlRepository()).isEqualTo(url);
    }

    @Test
    @DisplayName("Should accept a repository URL with the www prefix")
    void shouldAcceptUrlWithWwwPrefix() {
        String owner = "PetersonSantos9K";
        String repo = "repository-teste-docflow";
        String url = "https://www.github.com/" + owner + "/" + repo;

        var obj = urlParser.validateAndExtract(url);

        assertThat(obj.owner()).isEqualTo(owner);
        assertThat(obj.repo()).isEqualTo(repo);
        assertThat(obj.cloneUrl())
                .isEqualTo("https://github.com/" + owner + "/" + repo);
        assertThat(obj.urlRepository()).isEqualTo(url);
    }

    @Test
    @DisplayName("Should extract the repository subpath")
    void shouldExtractRepositorySubpath() {
        String owner = "PetersonSantos9K";
        String repo = "repository-teste-docflow";
        String subPath = "src/main/java";
        String url = "https://github.com/" + owner + "/" + repo + "/" + subPath;

        var obj = urlParser.validateAndExtract(url);

        assertThat(obj.owner()).isEqualTo(owner);
        assertThat(obj.repo()).isEqualTo(repo);
        assertThat(obj.subPath()).isEqualTo(subPath);
        assertThat(obj.cloneUrl())
                .isEqualTo("https://github.com/" + owner + "/" + repo);
        assertThat(obj.urlRepository()).isEqualTo(url);
    }

    @Test
    @DisplayName("Should reject a null repository URL")
    void shouldRejectNullRepositoryUrl() {
        String url = null;

        assertThatThrownBy(() ->
                urlParser.validateAndExtract(url)
        )
                .isInstanceOf(SourceException.class)
                .hasMessage("Repository URL must not be empty.");
    }

    @Test
    @DisplayName("Should reject a URL without owner and repository")
    void shouldRejectUrlWithoutOwnerAndRepository() {
        String url = "https://github.com/";

        assertThatThrownBy(() ->
                urlParser.validateAndExtract(url)
        )
                .isInstanceOf(SourceException.class)
                .hasMessage("Repository URL must include both owner and repository name.");
    }

    @Test
    @DisplayName("Should reject a blank repository URL")
    void shouldRejectBlankRepositoryUrl() {
        String url = "   ";

        assertThatThrownBy(() ->
                urlParser.validateAndExtract(url)
        )
                .isInstanceOf(SourceException.class)
                .hasMessage("Repository URL must not be empty.");
    }


    @Test
    @DisplayName("Should reject a repository URL exceeding the maximum length")
    void shouldRejectUrlExceedingMaxLength(){
        String url = "https://github.com/PetersonSantos9K/" + "a".repeat(165);
        assertThat(url).hasSize(201);
        assertThatThrownBy(() ->
            urlParser.validateAndExtract(url)
        )
                .isInstanceOf(SourceException.class)
                .hasMessage("Repository URL exceeds the maximum length of 200 characters.");
    }

    @Test
    @DisplayName("Should reject a repository URL with an unsupported prefix")
    void shouldRejectUrlWithUnsupportedPrefix() {
        String url = "https://gitlab.com/PetersonSantos9K/repository-teste-docflow";

        assertThatThrownBy(() ->
                urlParser.validateAndExtract(url)
        )
                .isInstanceOf(SourceException.class)
                .hasMessage("Unsupported repository URL. Only GitHub HTTPS URLs are supported.");
    }

    @Test
    @DisplayName("Should reject a repository URL with an invalid owner")
    void shouldRejectUrlWithInvalidOwner() {
        String url = "https://github.com/Peterson--Santos/repository-teste-docflow";

        assertThatThrownBy(() ->
                urlParser.validateAndExtract(url)
        )
                .isInstanceOf(SourceException.class)
                .hasMessage("Invalid GitHub owner in repository URL: 'Peterson--Santos'.");
    }

    @Test
    @DisplayName("Should reject a repository URL with an invalid repository path")
    void shouldRejectUrlWithInvalidPath() {
        String url = "https://github.com/PetersonSantos9K/repository teste";

        assertThatThrownBy(() ->
                urlParser.validateAndExtract(url)
        )
                .isInstanceOf(SourceException.class)
                .hasMessage("Invalid repository path in repository URL: 'repository teste'.");
    }

    @Test
    @DisplayName("Should reject a repository name equal to dot")
    void shouldRejectRepositoryNameEqualToDot() {
        String url = "https://github.com/PetersonSantos9K/.";

        assertThatThrownBy(() ->
                urlParser.validateAndExtract(url)
        )
                .isInstanceOf(SourceException.class)
                .hasMessage("Invalid repository name in repository URL: '.'.");
    }

    @Test
    @DisplayName("Should reject a repository name equal to .git")
    void shouldRejectRepositoryNameEqualToGitDirectory() {
        String url = "https://github.com/PetersonSantos9K/.git";

        assertThatThrownBy(() ->
                urlParser.validateAndExtract(url)
        )
                .isInstanceOf(SourceException.class)
                .hasMessage("Invalid repository name in repository URL: '.git'.");
    }

    @Test
    @DisplayName("Should accept a hidden subpath")
    void shouldAcceptHiddenSubPath() {
        String url = "https://github.com/PetersonSantos9K/repository-teste-docflow/.github/workflows";

        var obj = urlParser.validateAndExtract(url);

        assertThat(obj.subPath()).isEqualTo(".github/workflows");
    }

    @Test
    @DisplayName("Should reject a subpath with a parent directory segment")
    void shouldRejectSubPathWithParentDirectory() {
        String url = "https://github.com/PetersonSantos9K/repository-teste-docflow/../etc";

        assertThatThrownBy(() ->
                urlParser.validateAndExtract(url)
        )
                .isInstanceOf(SourceException.class)
                .hasMessage("Invalid repository path in repository URL: "
                        + "'repository-teste-docflow/../etc'.");
    }

    @Test
    @DisplayName("Should reject a subpath with a current directory segment")
    void shouldRejectSubPathWithCurrentDirectory() {
        String url = "https://github.com/PetersonSantos9K/repository-teste-docflow/./src";

        assertThatThrownBy(() ->
                urlParser.validateAndExtract(url)
        )
                .isInstanceOf(SourceException.class)
                .hasMessage("Invalid repository path in repository URL: "
                        + "'repository-teste-docflow/./src'.");
    }

    @Test
    @DisplayName("Should reject a subpath with an empty segment")
    void shouldRejectSubPathWithEmptySegment() {
        String url = "https://github.com/PetersonSantos9K/repository-teste-docflow//src";

        assertThatThrownBy(() ->
                urlParser.validateAndExtract(url)
        )
                .isInstanceOf(SourceException.class)
                .hasMessage("Invalid repository path in repository URL: "
                        + "'repository-teste-docflow//src'.");
    }

    @Test
    @DisplayName("Should reject GitHub tree, blob and commit paths")
    void shouldRejectUnsupportedGitHubPaths() {
        assertUnsupportedGitHubPath("tree/main/src");
        assertUnsupportedGitHubPath("blob/main/README.md");
        assertUnsupportedGitHubPath("commit/abc123");
    }

    private void assertUnsupportedGitHubPath(String path) {
        String url = "https://github.com/PetersonSantos9K/repository-teste-docflow/" + path;

        assertThatThrownBy(() ->
                urlParser.validateAndExtract(url)
        )
                .isInstanceOf(SourceException.class)
                .hasMessage("GitHub /tree, /blob and /commit URLs are not supported. "
                        + "Use a directory path instead.");
    }

}