package com.docflow.pipeline.client;

import com.docflow.extraction.domain.model.ProjectModel;
import com.docflow.extraction.orchestrator.ExtractionImpl;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class ExtractionOrchestratorClient {

    private final ExtractionImpl extractionImpl;

    public ExtractionOrchestratorClient(
            ExtractionImpl extractionImpl) {
        this.extractionImpl = extractionImpl;
    }

    public ProjectModel extract(Path sourceRoot) {
        return extractionImpl.extract(sourceRoot);
    }
}
