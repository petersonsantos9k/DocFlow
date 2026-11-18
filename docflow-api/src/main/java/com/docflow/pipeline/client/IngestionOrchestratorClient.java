package com.docflow.pipeline.client;

import com.docflow.ingestion.orchestrator.IngestionImpl;
import com.docflow.ingestion.orchestrator.IngestionSession;
import org.springframework.stereotype.Component;

@Component
public class IngestionOrchestratorClient {

    private final IngestionImpl ingestionService;

    public IngestionOrchestratorClient(IngestionImpl ingestionService) {
        this.ingestionService = ingestionService;
    }

    public IngestionSession ingest(String repositoryUrl) {
        return ingestionService.ingest(repositoryUrl);
    }
}
