package com.docflow.ingestion.domain.model;

public record RepositorySource(
        String owner,
        String repo,
        String subPath,
        String cloneUrl,
        String urlRepository
) {
}
