package com.docflow.extraction.domain.model;

import java.util.List;

public record ControllerInfo(
        String name,
        String packageName,
        String basePath,
        List<EndpointInfo> endpoints
) {
}
