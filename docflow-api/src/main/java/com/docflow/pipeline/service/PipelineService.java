package com.docflow.pipeline.service;

import com.docflow.extraction.domain.model.ProjectModel;
import com.docflow.ingestion.orchestrator.IngestionSession;
import com.docflow.pipeline.client.ExtractionOrchestratorClient;
import com.docflow.pipeline.client.IngestionOrchestratorClient;
import org.springframework.stereotype.Service;

@Service
public class PipelineService {

    private final IngestionOrchestratorClient ingestionClient;
    private final ExtractionOrchestratorClient extractionClient;

    public PipelineService(
            IngestionOrchestratorClient ingestionClient,
            ExtractionOrchestratorClient extractionClient) {
        this.ingestionClient = ingestionClient;
        this.extractionClient = extractionClient;
    }

    public ProjectModel run(String repositoryUrl) {
        try (IngestionSession session = ingestionClient.ingest(repositoryUrl)) {
            return extractionClient.extract(session.sourceRoot());
        }
    }
}
