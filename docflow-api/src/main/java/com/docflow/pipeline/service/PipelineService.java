package com.docflow.pipeline.service;

import com.docflow.documentation.DocumentationImpl;
import com.docflow.extraction.domain.model.ProjectModel;
import com.docflow.extraction.orchestrator.ExtractionImpl;
import com.docflow.ingestion.orchestrator.IngestionImpl;
import com.docflow.ingestion.orchestrator.IngestionSession;
import com.docflow.pipeline.dto.response.PipelineResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class PipelineService {

    private final IngestionImpl ingestionImpl;
    private final ExtractionImpl extractionImpl;
    private final DocumentationImpl documentationImpl;

    public PipelineResponse run(String repositoryUrl) {

        ProjectModel model;

        try (IngestionSession session = ingestionImpl.ingest(repositoryUrl)) {
            model = extractionImpl.extract(session.sourceRoot());
        }
        String apiGuide = documentationImpl.generateApiGuide(model);
        saveToDisk(model.info().name(), apiGuide);
        return new PipelineResponse(model, apiGuide);
    }

    private void saveToDisk(String projectName, String content) {
        try {
            Path dir = Path.of("output", projectName.replaceAll("[^a-zA-Z0-9._-]", "_"));
            Files.createDirectories(dir);
            Files.writeString(dir.resolve("api-guide.md"), content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save api-guide.md", e);
        }
    }
}
