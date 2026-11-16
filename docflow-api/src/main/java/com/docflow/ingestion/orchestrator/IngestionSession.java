package com.docflow.ingestion.orchestrator;

import com.docflow.ingestion.domain.model.IngestionContext;
import com.docflow.ingestion.domain.model.WorkspaceInfo;
import com.docflow.ingestion.workspace.WorkspaceProvider;

import java.nio.file.Path;

public final class IngestionSession implements AutoCloseable {

    private final IngestionContext context;
    private final WorkspaceInfo workspaceInfo;
    private final WorkspaceProvider workspaceProvider;

    private boolean closed;

    IngestionSession(
            IngestionContext context,
            WorkspaceInfo workspaceInfo,
            WorkspaceProvider workspaceProvider) {
        this.context = context;
        this.workspaceInfo = workspaceInfo;
        this.workspaceProvider = workspaceProvider;
    }

    public IngestionContext context() {
        return context;
    }

    public Path workspacePath() {
        return context.workspacePath();
    }

    public Path sourceRoot() {
        return context.sourceRoot();
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
        workspaceProvider.deleteWorkspace(workspaceInfo);
    }
}
