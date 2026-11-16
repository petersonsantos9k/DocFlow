package com.docflow.ingestion.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.util.unit.DataSize;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "docflow.ingestion")
public class IngestionProperties {

    private Path workspaceRoot = Path.of(
            System.getProperty("java.io.tmpdir"),
            "docflow",
            "jobs"
    );

    private DataSize maxRepositorySize = DataSize.ofMegabytes(100);

    public Path getWorkspaceRoot() {
        return workspaceRoot;
    }

    public void setWorkspaceRoot(Path workspaceRoot) {
        this.workspaceRoot = workspaceRoot;
    }

    public DataSize getMaxRepositorySize() {
        return maxRepositorySize;
    }

    public void setMaxRepositorySize(DataSize maxRepositorySize) {
        this.maxRepositorySize = maxRepositorySize;
    }
}
