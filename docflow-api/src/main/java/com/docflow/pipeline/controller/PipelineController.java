package com.docflow.pipeline.controller;

import com.docflow.extraction.domain.model.ProjectModel;
import com.docflow.pipeline.dto.request.PipelineRequest;
import com.docflow.pipeline.service.PipelineService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pipeline")
public class PipelineController {

    private final PipelineService pipelineService;

    public PipelineController(PipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    @PostMapping
    public ProjectModel run(@Valid @RequestBody PipelineRequest request) {
        return pipelineService.run(request.repositoryUrl());
    }
}
