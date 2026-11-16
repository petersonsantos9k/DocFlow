package com.docflow;

import com.docflow.extraction.domain.model.ControllerInfo;
import com.docflow.extraction.domain.model.EndpointInfo;
import com.docflow.extraction.domain.model.FieldInfo;
import com.docflow.extraction.domain.model.ParameterInfo;
import com.docflow.extraction.domain.model.ProjectInfo;
import com.docflow.extraction.domain.model.ProjectInfoSpringBoot;
import com.docflow.extraction.domain.model.ProjectModel;
import com.docflow.extraction.domain.model.SchemaInfo;
import org.springframework.stereotype.Component;

@Component
public class ProjectModelPrinter {

    public void print(ProjectModel<?> projectModel) {
        IO.println("========== PROJECT MODEL ==========");
        printProject(projectModel.project());

        IO.println();
        IO.println("Controllers: " + projectModel.controllers().size());
        for (ControllerInfo controller : projectModel.controllers()) {
            printController(controller);
        }

        IO.println();
        IO.println("Schemas: " + projectModel.schemas().size());
        for (SchemaInfo schema : projectModel.schemas()) {
            printSchema(schema);
        }
    }

    private void printProject(ProjectInfo<?> project) {
        if (project == null) {
            IO.println("Project: not available");
            return;
        }

        IO.println("Project: " + project.name());
        IO.println("  Build tool:   " + project.buildTool());
        IO.println("  Java version: " + project.javaVersion());

        if (project.type() instanceof ProjectInfoSpringBoot springBoot) {
            IO.println("  Spring Boot:  " + springBoot.springBootVersion());
            IO.println("  Dependencies: " + springBoot.dependencies());
        } else {
            IO.println("  Type:         " + project.type());
        }
    }

    private void printController(ControllerInfo controller) {
        IO.println();
        IO.println("Controller: " + controller.name());
        IO.println("  Package:   " + controller.packageName());
        IO.println("  BasePath:  " + controller.basePath());
        IO.println("  Endpoints: " + controller.endpoints().size());

        for (EndpointInfo endpoint : controller.endpoints()) {
            printEndpoint(endpoint);
        }
    }

    private void printEndpoint(EndpointInfo endpoint) {
        IO.println();
        IO.println("  " + endpoint.httpMethod() + " " + endpoint.path());
        IO.println("    method:     " + endpoint.methodName());
        IO.println("    returnType: " + endpoint.returnType());
        IO.println("    status:     " + (endpoint.status() == null ? "-" : endpoint.status()));
        IO.println("    dtos:       " + endpoint.dtos());

        if (endpoint.parameters().isEmpty()) {
            IO.println("    parameters: (none)");
            return;
        }

        IO.println("    parameters:");
        for (ParameterInfo parameter : endpoint.parameters()) {
            IO.println("      - " + parameter.name()
                    + " (" + parameter.location()
                    + ", " + parameter.type()
                    + ", required=" + parameter.required() + ")");
        }
    }

    private void printSchema(SchemaInfo schema) {
        IO.println();
        IO.println("Schema: " + schema.name());
        IO.println("  kind:   " + schema.kind());
        IO.println("  fields: " + schema.fields().size());

        for (FieldInfo field : schema.fields()) {
            IO.println("    - " + field.name()
                    + ": " + field.type()
                    + " " + field.validations());
        }
    }
}
