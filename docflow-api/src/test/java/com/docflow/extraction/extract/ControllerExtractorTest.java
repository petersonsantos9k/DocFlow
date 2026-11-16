package com.docflow.extraction.extract;

import com.docflow.extraction.domain.model.ControllerInfo;
import com.docflow.extraction.domain.model.EndpointInfo;
import com.docflow.extraction.domain.model.ParameterInfo;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ControllerExtractorTest {

    private final ControllerExtractor controllerExtractor = new ControllerExtractor();

    @Test
    @DisplayName("Should extract named paths, arrays, required flag, status and DTOs")
    void shouldExtractEndpointDetails() {
        CompilationUnit controller = parse("UserController.java", """
                package com.example.user;

                import com.example.user.dto.UserRequestDTO;
                import com.example.user.dto.UserResponseDTO;
                import org.springframework.http.ResponseEntity;
                import org.springframework.web.bind.annotation.*;

                @RestController
                @RequestMapping(path = "/api/users")
                public class UserController {

                    @PostMapping(path = {"/create", "/new"})
                    @ResponseStatus(HttpStatus.CREATED)
                    public ResponseEntity<UserResponseDTO> create(
                            @RequestBody UserRequestDTO request) {
                        return null;
                    }

                    @GetMapping("/{id}")
                    public UserResponseDTO find(
                            @PathVariable String id,
                            @RequestParam(required = false) String expand) {
                        return null;
                    }
                }
                """);

        CompilationUnit requestDto = parse("UserRequestDTO.java", """
                package com.example.user.dto;
                public class UserRequestDTO {
                    private String name;
                }
                """);

        CompilationUnit responseDto = parse("UserResponseDTO.java", """
                package com.example.user.dto;
                public record UserResponseDTO(String id) {
                }
                """);

        TypeIndex typeIndex = TypeIndex.from(
                List.of(controller, requestDto, responseDto));

        ControllerInfo controllerInfo = controllerExtractor.extract(controller, typeIndex);

        assertThat(controllerInfo.name()).isEqualTo("UserController");
        assertThat(controllerInfo.packageName()).isEqualTo("com.example.user");
        assertThat(controllerInfo.basePath()).isEqualTo("/api/users");
        assertThat(controllerInfo.endpoints()).hasSize(2);

        EndpointInfo create = endpoint(controllerInfo, "create");
        assertThat(create.httpMethod()).isEqualTo("POST");
        assertThat(create.path()).isEqualTo("/api/users/create");
        assertThat(create.status()).isEqualTo(201);
        assertThat(create.dtos())
                .containsExactly("UserRequestDTO", "UserResponseDTO");
        assertThat(create.parameters())
                .extracting(ParameterInfo::name, ParameterInfo::required)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("request", true));

        EndpointInfo find = endpoint(controllerInfo, "find");
        assertThat(find.httpMethod()).isEqualTo("GET");
        assertThat(find.path()).isEqualTo("/api/users/{id}");
        assertThat(find.dtos()).containsExactly("UserResponseDTO");
        assertThat(find.parameters())
                .extracting(ParameterInfo::name, ParameterInfo::required)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("id", true),
                        org.assertj.core.groups.Tuple.tuple("expand", false));
    }

    private CompilationUnit parse(String fileName, String source) {
        ParserConfiguration configuration = new ParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_25);
        CompilationUnit compilationUnit = new JavaParser(configuration).parse(source)
                .getResult()
                .orElseThrow();
        compilationUnit.setStorage(java.nio.file.Path.of(fileName));
        return compilationUnit;
    }

    private EndpointInfo endpoint(ControllerInfo controller, String methodName) {
        return controller.endpoints().stream()
                .filter(endpoint -> endpoint.methodName().equals(methodName))
                .findFirst()
                .orElseThrow();
    }
}
