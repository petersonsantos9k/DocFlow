package com.docflow.extraction.domain.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

public record ValidationInfo(
        String name,
        Map<String, String> attributes
) {

    public ValidationInfo {
        attributes = Collections.unmodifiableMap(new LinkedHashMap<>(attributes));
    }

    @Override
    public String toString() {
        if (attributes.isEmpty()) {
            return name;
        }

        StringJoiner joiner = new StringJoiner(", ", "(", ")");
        attributes.forEach((key, value) -> joiner.add(key + "=" + value));
        return name + joiner;
    }
}
