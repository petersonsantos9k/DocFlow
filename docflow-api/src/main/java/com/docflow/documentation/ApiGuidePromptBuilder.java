package com.docflow.documentation;

import com.docflow.extraction.domain.model.ControllerInfo;
import com.docflow.extraction.domain.model.SchemaInfo;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Component
class ApiGuidePromptBuilder {

    private final ObjectMapper mapper;

    ApiGuidePromptBuilder(ObjectMapper mapper){
        this.mapper = mapper;
    }

    String forController(ControllerInfo controller, List<SchemaInfo> schemas) {
        return """
                <controller>
                %s
                </controller>

                <schemas>
                %s
                </schemas>
                """.formatted(toJson(controller), toJson(schemas));
    }

    private String toJson(Object value) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("Could not serialize prompt data", e);
        }
    }
}
