package com.docflow.pipeline.dto.response;

import com.docflow.extraction.domain.model.ProjectModel;

public record PipelineResponse(
        ProjectModel model,
        String apiGuide) {

}
