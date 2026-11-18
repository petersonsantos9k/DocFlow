package com.docflow.pipeline.client;

import com.docflow.extraction.domain.model.ProjectModel;
import com.docflow.extraction.orchestrator.ExtractionOrchestrator;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class ExtractionOrchestratorClient {

    private final ExtractionOrchestrator extractionOrchestrator;

    public ExtractionOrchestratorClient(
            ExtractionOrchestrator extractionOrchestrator) {
        this.extractionOrchestrator = extractionOrchestrator;
    }

    public ProjectModel extract(Path sourceRoot) {
        return extractionOrchestrator.extract(sourceRoot);
    }
}
