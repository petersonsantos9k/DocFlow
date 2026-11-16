package com.docflow.ingestion.source;

import com.docflow.ingestion.client.github.GitHubClientProperties;
import com.docflow.ingestion.domain.exception.SourceException;
import com.docflow.ingestion.domain.model.RepositorySource;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component("jGitSourceProvider")
public class GitRepositorySourceProvider implements SourceProvider {

    private final GitHubRepositoryUrlParser urlParser;
    private final GitHubClientProperties gitHubClientProperties;

    public GitRepositorySourceProvider(
            GitHubRepositoryUrlParser urlParser,
            GitHubClientProperties gitHubClientProperties){
        this.urlParser = urlParser;
        this.gitHubClientProperties = gitHubClientProperties;
    }

    @Override
    public RepositorySource fromRepositoryUrl(String repositoryUrl) {
        return urlParser.validateAndExtract(repositoryUrl);
    }

    @Override
    public void download(RepositorySource sourceInfo, Path workspacePath) {

        var clone = Git.cloneRepository()
                .setURI(sourceInfo.cloneUrl())
                .setDirectory(workspacePath.toFile())
                .setDepth(1)
                .setCloneSubmodules(false);

        String token = gitHubClientProperties.getToken();
        if (token != null && !token.isBlank()) {
            clone.setCredentialsProvider(
                    new UsernamePasswordCredentialsProvider("x-access-token", token));
        }

        try(Git git = clone.call()){

            if(!Files.isDirectory(workspacePath)) {
                throw new SourceException(
                        "Downloaded repository directory does not exist: '"
                                + workspacePath + "'.");
            }

            if(git.getRepository().resolve(Constants.HEAD) == null) {
                throw new SourceException(
                        "Repository '" + sourceInfo.owner() + "/" + sourceInfo.repo()
                                + "' is empty: no commits found.");
            }
        } catch (SourceException err) {
            throw err;
        } catch (GitAPIException | IOException err) {
            throw new SourceException(
                    "Failed to clone repository '" + sourceInfo.owner() + "/"
                            + sourceInfo.repo() + "'.", err);
        }

    }
}
