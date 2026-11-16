package com.docflow.extraction.domain.model;

public record ProjectInfo <Type>(
        String name,
        String buildTool,
        String javaVersion,
        Type type
) {
}
