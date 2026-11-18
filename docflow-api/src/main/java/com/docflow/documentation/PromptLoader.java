package com.docflow.documentation;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
class PromptLoader {
    protected String load(String fileName) {
        try (InputStream in = new ClassPathResource("prompts/" + fileName).getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Prompt not found: " + fileName, e);
        }
    }
}
