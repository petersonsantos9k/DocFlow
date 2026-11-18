package com.docflow.pipeline.client;

import com.docflow.ingestion.orchestrator.IngestionService;
import com.docflow.ingestion.orchestrator.IngestionSession;
import org.springframework.stereotype.Component;

@Component
public class IngestionOrchestratorClient {

    private final IngestionService ingestionService;

    public IngestionOrchestratorClient(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    public IngestionSession ingest(String repositoryUrl) {
        return ingestionService.ingest(repositoryUrl);
    }
}
