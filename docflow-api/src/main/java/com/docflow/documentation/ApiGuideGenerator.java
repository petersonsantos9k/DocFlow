package com.docflow.documentation;

import com.docflow.extraction.domain.model.ControllerInfo;
import com.docflow.extraction.domain.model.ProjectModel;
import com.docflow.llm.LlmClient;
import org.springframework.stereotype.Component;

@Component
class ApiGuideGenerator {

    private final LlmClient llm;
    private final ApiGuidePromptBuilder promptBuilder;
    private final String systemPrompt;

    ApiGuideGenerator(LlmClient llm, ApiGuidePromptBuilder promptBuilder, PromptLoader loader) {
        this.llm = llm;
        this.promptBuilder = promptBuilder;
        this.systemPrompt = loader.load("api-guide-system.txt");   // carregado uma vez
    }

    String generate(ProjectModel model) {
        StringBuilder md = new StringBuilder();
        md.append("# ").append(model.info().name()).append(" - API Guide\n\n");

        for (ControllerInfo controller : model.controllers()) {
            String raw = llm.generate(systemPrompt, promptBuilder.forController(controller, model.schemas()));
            md.append(MarkdownCleaner.clean(raw)).append("\n\n");
        }
        return md.toString();
    }
}