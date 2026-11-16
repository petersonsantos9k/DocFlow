package com.docflow;

import com.docflow.extraction.domain.model.ProjectInfoSpringBoot;
import com.docflow.extraction.domain.model.ProjectModel;
import com.docflow.extraction.orchestrator.ExtractionOrchestrator;
import com.docflow.ingestion.orchestrator.IngestionService;
import com.docflow.ingestion.orchestrator.IngestionSession;
import org.springframework.stereotype.Component;

@Component
public class DemoRunner {

    private static final String REPOSITORY_URL =
            "https://github.com/petersonsantos9k/picpay-desafio-backend";

    private final IngestionService ingestionService;
    private final ExtractionOrchestrator extractionOrchestrator;
    private final ProjectModelPrinter projectModelPrinter;

    public DemoRunner(
            IngestionService ingestionService,
            ExtractionOrchestrator extractionOrchestrator,
            ProjectModelPrinter projectModelPrinter) {
        this.ingestionService = ingestionService;
        this.extractionOrchestrator = extractionOrchestrator;
        this.projectModelPrinter = projectModelPrinter;
    }

    public void run() {
        try (IngestionSession session = ingestionService.ingest(REPOSITORY_URL)) {
            ProjectModel<ProjectInfoSpringBoot> projectModel =
                    extractionOrchestrator.extract(session.sourceRoot());

            projectModelPrinter.print(projectModel);
        }
    }
}
