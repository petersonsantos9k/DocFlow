package com.docflow.extraction.domain.model;

import java.util.Set;

public record ProjectInfo(
        String name,
        String buildTool,
        String javaVersion,
        String springBootVersion,
        Set<String> dependencies
) {
}
