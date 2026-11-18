package com.docflow.pipeline.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PipelineRequest(
        @NotBlank String repositoryUrl
) {
}
