package com.docflow.documentation;

import com.docflow.extraction.domain.model.ProjectModel;
import org.springframework.stereotype.Component;

@Component
public class DocumentationImpl {

    private final ApiGuideGenerator apiGuideGenerator;

    public DocumentationImpl(ApiGuideGenerator apiGuideGenerator) {
        this.apiGuideGenerator = apiGuideGenerator;
    }

    public String generateApiGuide(ProjectModel model){
        return apiGuideGenerator.generate(model);
    }


}
