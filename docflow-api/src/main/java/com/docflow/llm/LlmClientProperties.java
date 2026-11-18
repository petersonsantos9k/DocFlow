package com.docflow.llm;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "docflow.llm")
public record LlmClientProperties(
        String baseUrl,
        String apiKey,
        String model,
        Duration timeout,
        Double temperature
) {}
