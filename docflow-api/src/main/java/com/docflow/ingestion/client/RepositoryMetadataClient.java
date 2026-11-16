package com.docflow.ingestion.client;

import com.docflow.ingestion.domain.model.GitHubRepositoryInfo;

import java.util.Optional;

public interface RepositoryMetadataClient {

    Optional<GitHubRepositoryInfo> getRepository(String owner, String repo);

}
