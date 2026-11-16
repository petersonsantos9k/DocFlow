package com.docflow.extraction.orchestrator;

import com.docflow.extraction.classifier.AnnotationClassifier;
import com.docflow.extraction.classifier.JavaClassClassifier;
import com.docflow.extraction.domain.model.ControllerInfo;
import com.docflow.extraction.domain.model.EndpointInfo;
import com.docflow.extraction.domain.model.FieldInfo;
import com.docflow.extraction.domain.model.ProjectInfo;
import com.docflow.extraction.domain.model.ProjectInfoSpringBoot;
import com.docflow.extraction.domain.model.ProjectModel;
import com.docflow.extraction.domain.model.SchemaInfo;
import com.docflow.extraction.extract.ControllerExtractor;
import com.docflow.extraction.extract.DtoExtractor;
import com.docflow.extraction.extract.ProjectInfoExtractor;
import com.docflow.extraction.scanner.JavaSourceAnalyzer;
import com.docflow.extraction.scanner.JavaSourceFileScanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class ExtractionOrchestratorTest {

    @TempDir
    Path tempDir;

    private ExtractionOrchestrator extractionOrchestrator;

    @BeforeEach
    void setUp() {
        extractionOrchestrator = new ExtractionOrchestrator(
                new JavaSourceFileScanner(),
                new JavaSourceAnalyzer(),
                new JavaClassClassifier(new AnnotationClassifier()),
                new ControllerExtractor(),
                new DtoExtractor(),
                new ProjectInfoExtractor()
        );
    }

    @Test
    @DisplayName("Should extract controllers, endpoint DTOs and schemas with validations")
    void shouldExtractControllersDtosAndSchemas() throws IOException {
        writeSource("com/example/user/UserController.java", """
                package com.example.user;

                import com.example.user.dto.UserRequestDTO;
                import com.example.user.dto.UserResponseDTO;
                import org.springframework.http.ResponseEntity;
                import org.springframework.web.bind.annotation.*;

                import java.util.List;

                @RestController
                @RequestMapping(path = "/api/users")
                public class UserController {

                    @PostMapping
                    public ResponseEntity<UserResponseDTO> create(
                            @RequestBody UserRequestDTO request) {
                        return null;
                    }

                    @GetMapping("/{id}")
                    public UserResponseDTO findById(@PathVariable String id) {
                        return null;
                    }

                    @GetMapping(path = "/search")
                    public List<UserResponseDTO> search(
                            @RequestParam(required = false) String name) {
                        return null;
                    }
                }
                """);

        writeSource("com/example/user/dto/UserRequestDTO.java", """
                package com.example.user.dto;

                import jakarta.validation.constraints.Email;
                import jakarta.validation.constraints.Max;
                import jakarta.validation.constraints.Min;
                import jakarta.validation.constraints.NotBlank;

                public class UserRequestDTO {

                    @NotBlank
                    private String name;

                    @Email
                    private String email;

                    @Min(18)
                    @Max(120)
                    private Integer age;

                    private static final String IGNORED = "ignored";
                }
                """);

        writeSource("com/example/user/dto/UserResponseDTO.java", """
                package com.example.user.dto;

                import java.util.UUID;

                public record UserResponseDTO(
                        UUID id,
                        String name,
                        String email) {
                }
                """);

        writeRootFile("pom.xml", """
                <project>
                    <modelVersion>4.0.0</modelVersion>
                    <parent>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-parent</artifactId>
                        <version>3.3.0</version>
                    </parent>
                    <artifactId>user-api</artifactId>
                    <name>User API</name>
                    <properties>
                        <java.version>21</java.version>
                    </properties>
                    <dependencies>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-web</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-validation</artifactId>
                        </dependency>
                    </dependencies>
                </project>
                """);

        ProjectModel<ProjectInfoSpringBoot> projectModel =
                extractionOrchestrator.extract(tempDir);

        ProjectInfo<ProjectInfoSpringBoot> project = projectModel.project();
        assertThat(project.name()).isEqualTo("User API");
        assertThat(project.buildTool()).isEqualTo("Maven");
        assertThat(project.javaVersion()).isEqualTo("21");
        assertThat(project.type().springBootVersion()).isEqualTo("3.3.0");
        assertThat(project.type().dependencies())
                .containsExactly("spring-boot-starter-web", "spring-boot-starter-validation");

        assertThat(projectModel.controllers()).hasSize(1);

        ControllerInfo controller = projectModel.controllers().getFirst();
        assertThat(controller.name()).isEqualTo("UserController");
        assertThat(controller.packageName()).isEqualTo("com.example.user");
        assertThat(controller.basePath()).isEqualTo("/api/users");
        assertThat(controller.endpoints()).hasSize(3);

        EndpointInfo createEndpoint = endpoint(controller, "create");
        assertThat(createEndpoint.httpMethod()).isEqualTo("POST");
        assertThat(createEndpoint.path()).isEqualTo("/api/users");
        assertThat(createEndpoint.dtos())
                .containsExactly("UserRequestDTO", "UserResponseDTO");

        EndpointInfo findEndpoint = endpoint(controller, "findById");
        assertThat(findEndpoint.httpMethod()).isEqualTo("GET");
        assertThat(findEndpoint.path()).isEqualTo("/api/users/{id}");
        assertThat(findEndpoint.dtos()).containsExactly("UserResponseDTO");

        EndpointInfo searchEndpoint = endpoint(controller, "search");
        assertThat(searchEndpoint.httpMethod()).isEqualTo("GET");
        assertThat(searchEndpoint.path()).isEqualTo("/api/users/search");
        assertThat(searchEndpoint.dtos()).containsExactly("UserResponseDTO");
        assertThat(searchEndpoint.parameters()).hasSize(1);
        assertThat(searchEndpoint.parameters().getFirst().required()).isFalse();

        Map<String, SchemaInfo> schemas = projectModel.schemas().stream()
                .collect(Collectors.toMap(SchemaInfo::name, Function.identity()));

        assertThat(schemas).containsKeys("UserRequestDTO", "UserResponseDTO");

        SchemaInfo requestSchema = schemas.get("UserRequestDTO");
        assertThat(requestSchema.kind()).isEqualTo("DTO");
        assertThat(requestSchema.fields())
                .extracting(FieldInfo::name)
                .containsExactlyInAnyOrder("name", "email", "age");
        assertThat(field(requestSchema, "name").validations()).containsExactly("NotBlank");
        assertThat(field(requestSchema, "email").validations()).containsExactly("Email");
        assertThat(field(requestSchema, "age").validations()).containsExactly("Min", "Max");

        SchemaInfo responseSchema = schemas.get("UserResponseDTO");
        assertThat(responseSchema.fields())
                .extracting(FieldInfo::name)
                .containsExactlyInAnyOrder("id", "name", "email");
    }

    @Test
    @DisplayName("Should skip invalid Java files and keep valid extraction")
    void shouldSkipInvalidJavaFiles() throws IOException {
        writeSource("com/example/Broken.java", "public class Broken {");
        writeSource("com/example/OkController.java", """
                package com.example;

                import org.springframework.web.bind.annotation.GetMapping;
                import org.springframework.web.bind.annotation.RestController;

                @RestController
                public class OkController {
                    @GetMapping("/ok")
                    public String ok() {
                        return "ok";
                    }
                }
                """);

        ProjectModel<ProjectInfoSpringBoot> projectModel =
                extractionOrchestrator.extract(tempDir);

        assertThat(projectModel.controllers()).hasSize(1);
        assertThat(projectModel.controllers().getFirst().name()).isEqualTo("OkController");
        assertThat(projectModel.controllers().getFirst().endpoints()).hasSize(1);
    }

    private EndpointInfo endpoint(ControllerInfo controller, String methodName) {
        return controller.endpoints().stream()
                .filter(endpoint -> endpoint.methodName().equals(methodName))
                .findFirst()
                .orElseThrow();
    }

    private FieldInfo field(SchemaInfo schema, String fieldName) {
        return schema.fields().stream()
                .filter(field -> field.name().equals(fieldName))
                .findFirst()
                .orElseThrow();
    }

    private void writeSource(String relativePath, String source) throws IOException {
        Path file = tempDir.resolve("src/main/java").resolve(relativePath);
        Files.createDirectories(file.getParent());
        Files.writeString(file, source);
    }

    private void writeRootFile(String relativePath, String content) throws IOException {
        Path file = tempDir.resolve(relativePath);
        Files.writeString(file, content);
    }
}
