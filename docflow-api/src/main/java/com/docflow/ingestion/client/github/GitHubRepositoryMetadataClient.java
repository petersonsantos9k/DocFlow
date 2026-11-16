package com.docflow.ingestion.client.github;

import com.docflow.ingestion.client.RepositoryMetadataClient;
import com.docflow.ingestion.domain.model.GitHubRepositoryInfo;
import com.docflow.ingestion.domain.exception.ClientException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Component("gitHubRepositoryClient")
public class GitHubRepositoryMetadataClient implements RepositoryMetadataClient {

    private final RestClient restClient;

    public GitHubRepositoryMetadataClient(@Qualifier("gitHubRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Optional<GitHubRepositoryInfo> getRepository(String owner, String repo) {

        try{
            return Optional.ofNullable(
                    restClient.get()
                            .uri("/repos/{owner}/{repo}", owner, repo)
                            .retrieve()
                            .body(GitHubRepositoryInfo.class)
            );
        } catch (HttpClientErrorException.NotFound e){
            return Optional.empty();
        } catch (HttpClientErrorException.Unauthorized e){
            throw new ClientException(
                    "GitHub rejected the configured token. Check the GITHUB_TOKEN value.", e);
        } catch (HttpClientErrorException.Forbidden e){
            if (isRateLimited(e)) {
                throw new ClientException(rateLimitMessage(owner, repo), e);
            }
            throw new ClientException(
                    "Access to repository '" + owner + "/" + repo + "' is forbidden.", e);
        } catch (HttpClientErrorException.TooManyRequests e){
            throw new ClientException(rateLimitMessage(owner, repo), e);
        } catch (ResourceAccessException e){
            throw new ClientException(
                    "Could not reach the GitHub API while checking '"
                            + owner + "/" + repo + "': " + e.getMessage(), e);
        } catch (RestClientException e){
            throw new ClientException(
                    "An error occurred while accessing the GitHub API for '"
                            + owner + "/" + repo + "'.", e);
        }
    }

    private boolean isRateLimited(HttpClientErrorException.Forbidden error) {
        var headers = error.getResponseHeaders();
        return "0".equals(headers.getFirst("X-RateLimit-Remaining"))
                || headers.getFirst("Retry-After") != null;
    }

    private String rateLimitMessage(String owner, String repo) {
        return "GitHub API rate limit exceeded for '" + owner + "/" + repo
                + "'. Configure a token or try again later.";
    }
}
