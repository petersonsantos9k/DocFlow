package com.docflow.ingestion.workspace;

import com.docflow.ingestion.config.IngestionProperties;
import com.docflow.ingestion.domain.exception.WorkspaceException;
import com.docflow.ingestion.domain.model.WorkspaceInfo;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Stream;

@Component
@Primary
public class TemporaryWorkspaceProvider implements WorkspaceProvider{

    private final Path workspaceRoot;

    public TemporaryWorkspaceProvider(IngestionProperties properties) {
        this.workspaceRoot = properties.getWorkspaceRoot()
                .toAbsolutePath()
                .normalize();
    }

    @Override
    public WorkspaceInfo createWorkspace(){
        UUID id = UUID.randomUUID();
        Path path = workspaceRoot.resolve(id.toString());
        try{
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new WorkspaceException("Failed to create workspace at '" + path + "'.", e);
        }

        return new WorkspaceInfo(id, path);
    }

    @Override
    public void deleteWorkspace(WorkspaceInfo workspaceLocation){
        try(Stream<Path> paths = Files.walk(workspaceLocation.path())) {

            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new WorkspaceException(
                                    "Failed to delete workspace at '"
                                            + workspaceLocation.path() + "'.", e);
                        }
                    });

        } catch (IOException e) {
            throw new WorkspaceException(
                    "Failed to delete workspace at '"
                            + workspaceLocation.path() + "'.", e);
        }
    }

}
