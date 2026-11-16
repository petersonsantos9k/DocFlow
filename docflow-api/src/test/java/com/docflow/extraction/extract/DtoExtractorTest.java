package com.docflow.extraction.extract;

import com.docflow.extraction.domain.model.FieldInfo;
import com.docflow.extraction.domain.model.SchemaInfo;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class DtoExtractorTest {

    private final DtoExtractor dtoExtractor = new DtoExtractor();

    @Test
    @DisplayName("Should extract class and record fields with validations")
    void shouldExtractClassAndRecordSchemas() {
        CompilationUnit requestDto = parse("""
                package com.example.dto;

                import jakarta.validation.constraints.Email;
                import jakarta.validation.constraints.NotBlank;
                import jakarta.validation.constraints.Size;

                public class UserRequestDTO {

                    @NotBlank
                    @Size(min = 3, max = 100)
                    private String name;

                    @Email
                    private String email;

                    private static final String IGNORED = "ignored";
                }
                """);

        CompilationUnit responseDto = parse("""
                package com.example.dto;

                import jakarta.validation.constraints.NotNull;

                public record UserResponseDTO(
                        @NotNull String id,
                        String name) {
                }
                """);

        TypeIndex typeIndex = TypeIndex.from(List.of(requestDto, responseDto));

        List<SchemaInfo> schemas = dtoExtractor.extract(
                List.of("UserRequestDTO", "UserResponseDTO"),
                typeIndex);

        Map<String, SchemaInfo> byName = schemas.stream()
                .collect(Collectors.toMap(SchemaInfo::name, Function.identity()));

        assertThat(byName).containsKeys("UserRequestDTO", "UserResponseDTO");

        SchemaInfo request = byName.get("UserRequestDTO");
        assertThat(request.kind()).isEqualTo("DTO");
        assertThat(request.fields())
                .extracting(FieldInfo::name)
                .containsExactlyInAnyOrder("name", "email");
        assertThat(field(request, "name").validations())
                .containsExactly("NotBlank", "Size");
        assertThat(field(request, "email").validations())
                .containsExactly("Email");

        SchemaInfo response = byName.get("UserResponseDTO");
        assertThat(response.fields())
                .extracting(FieldInfo::name)
                .containsExactlyInAnyOrder("id", "name");
        assertThat(field(response, "id").validations())
                .containsExactly("NotNull");
    }

    @Test
    @DisplayName("Should ignore unresolved DTO names")
    void shouldIgnoreUnresolvedDtoNames() {
        CompilationUnit requestDto = parse("""
                package com.example.dto;
                public class UserRequestDTO {
                    private String name;
                }
                """);

        TypeIndex typeIndex = TypeIndex.from(List.of(requestDto));

        List<SchemaInfo> schemas = dtoExtractor.extract(
                List.of("MissingDTO"),
                typeIndex);

        assertThat(schemas).isEmpty();
    }

    private CompilationUnit parse(String source) {
        ParserConfiguration configuration = new ParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_25);
        return new JavaParser(configuration).parse(source)
                .getResult()
                .orElseThrow();
    }

    private FieldInfo field(SchemaInfo schema, String fieldName) {
        return schema.fields().stream()
                .filter(field -> field.name().equals(fieldName))
                .findFirst()
                .orElseThrow();
    }
}
