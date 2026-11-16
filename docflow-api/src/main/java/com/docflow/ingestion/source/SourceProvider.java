package com.docflow.ingestion.source;

import com.docflow.ingestion.domain.model.RepositorySource;

import java.nio.file.Path;

public interface SourceProvider {

    RepositorySource fromRepositoryUrl(String repositoryUrl);
    void download(RepositorySource sourceInfo, Path workspacePath);

}
