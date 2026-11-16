package com.docflow;

import com.docflow.extraction.domain.model.ControllerInfo;
import com.docflow.extraction.domain.model.EndpointInfo;
import com.docflow.extraction.domain.model.FieldInfo;
import com.docflow.extraction.domain.model.ParameterInfo;
import com.docflow.extraction.domain.model.ProjectInfo;
import com.docflow.extraction.domain.model.ProjectInfoSpringBoot;
import com.docflow.extraction.domain.model.ProjectModel;
import com.docflow.extraction.domain.model.SchemaInfo;
import com.docflow.extraction.domain.model.ValidationInfo;
import com.docflow.extraction.domain.model.enums.ParameterLocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DemoRunnerTest {

    @Test
    @DisplayName("Should print all ProjectModel data")
    void shouldPrintProjectModel() {
        ProjectInfo<ProjectInfoSpringBoot> project = new ProjectInfo<>(
                "Demo API",
                "Maven",
                "21",
                new ProjectInfoSpringBoot("3.3.0", Set.of("spring-boot-starter-web")));

        ControllerInfo controller = new ControllerInfo(
                "UserController",
                "com.example.user",
                "/api/users",
                List.of(new EndpointInfo(
                        "create",
                        "POST",
                        "/api/users",
                        List.of(new ParameterInfo(
                                "request",
                                ParameterLocation.BODY,
                                "UserRequestDTO",
                                true)),
                        "ResponseEntity<UserResponseDTO>",
                        201,
                        List.of("UserRequestDTO", "UserResponseDTO"))));

        SchemaInfo schema = new SchemaInfo(
                "UserRequestDTO",
                "DTO",
                new LinkedHashSet<>(List.of(
                        new FieldInfo(
                                "name",
                                "String",
                                List.of(new ValidationInfo("NotBlank", Map.of()))),
                        new FieldInfo(
                                "email",
                                "String",
                                List.of(new ValidationInfo("Email", Map.of()))))));

        ProjectModel<ProjectInfoSpringBoot> projectModel = new ProjectModel<>(
                project,
                List.of(controller),
                List.of(schema));

        String output = captureOutput(() -> DemoRunner.printProjectModel(projectModel));

        assertThat(output)
                .contains("========== PROJECT MODEL ==========")
                .contains("Project: Demo API")
                .contains("Build tool:   Maven")
                .contains("Java version: 21")
                .contains("Spring Boot:  3.3.0")
                .contains("Controllers: 1")
                .contains("Controller: UserController")
                .contains("Package:   com.example.user")
                .contains("BasePath:  /api/users")
                .contains("POST /api/users")
                .contains("method:     create")
                .contains("status:     201")
                .contains("dtos:       [UserRequestDTO, UserResponseDTO]")
                .contains("request (BODY, UserRequestDTO, required=true)")
                .contains("Schemas: 1")
                .contains("Schema: UserRequestDTO")
                .contains("name: String [NotBlank]")
                .contains("email: String [Email]");
    }

    private String captureOutput(Runnable action) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try (PrintStream capture = new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            action.run();
        } finally {
            System.setOut(original);
        }

        return buffer.toString(StandardCharsets.UTF_8);
    }
}
