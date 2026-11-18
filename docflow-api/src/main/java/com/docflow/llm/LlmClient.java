package com.docflow.llm;

public interface LlmClient {
    String generate(String systemPrompt, String userPrompt);
}
