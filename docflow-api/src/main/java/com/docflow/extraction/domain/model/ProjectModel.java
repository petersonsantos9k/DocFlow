package com.docflow.extraction.domain.model;

import java.util.List;

public record ProjectModel(
        ProjectInfo info,
        List<ControllerInfo> controllers,
        List<SchemaInfo> schemas
) {
}
