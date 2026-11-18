package com.docflow.documentation;

final class MarkdownCleaner {

    private MarkdownCleaner() {}

    static String clean(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String text = raw.strip();
        if (text.startsWith("```")) {
            int firstLineEnd = text.indexOf('\n');
            int lastFence = text.lastIndexOf("```");
            if (firstLineEnd > 0 && lastFence > firstLineEnd) {
                text = text.substring(firstLineEnd + 1, lastFence).strip();
            }
        }
        return text;
    }
}