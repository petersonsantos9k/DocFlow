package com.docflow.extraction.extract;

import com.docflow.extraction.domain.model.FieldInfo;
import com.docflow.extraction.domain.model.SchemaInfo;
import com.docflow.extraction.domain.model.ValidationInfo;
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
                .containsExactly(
                        new ValidationInfo("NotBlank", Map.of()),
                        new ValidationInfo("Size", Map.of("min", "3", "max", "100")));
        assertThat(field(request, "email").validations())
                .containsExactly(new ValidationInfo("Email", Map.of()));

        SchemaInfo response = byName.get("UserResponseDTO");
        assertThat(response.fields())
                .extracting(FieldInfo::name)
                .containsExactlyInAnyOrder("id", "name");
        assertThat(field(response, "id").validations())
                .containsExactly(new ValidationInfo("NotNull", Map.of()));
    }

    @Test
    @DisplayName("Should extract validation argument values")
    void shouldExtractValidationArguments() {
        CompilationUnit requestDto = parse("""
                package com.example.dto;

                import jakarta.validation.constraints.DecimalMin;
                import jakarta.validation.constraints.Digits;
                import jakarta.validation.constraints.Min;
                import jakarta.validation.constraints.NotNull;
                import jakarta.validation.constraints.Pattern;
                import jakarta.validation.constraints.Size;

                import java.math.BigDecimal;

                public record TransferRequestDTO(
                        @NotNull(message = "Transfer amount is required")
                        @DecimalMin(
                                value = "0.01",
                                message = "must be greater than zero")
                        @Digits(integer = 10, fraction = 2)
                        BigDecimal value,

                        @Size(min = 3, max = 100)
                        String name,

                        @Pattern(regexp = "\\\\d{11}|\\\\d{14}")
                        String taxId,

                        @Min(18)
                        Integer age) {
                }
                """);

        TypeIndex typeIndex = TypeIndex.from(List.of(requestDto));

        SchemaInfo schema = dtoExtractor.extract(
                        List.of("TransferRequestDTO"), typeIndex)
                .getFirst();

        assertThat(field(schema, "value").validations())
                .containsExactly(
                        new ValidationInfo(
                                "NotNull",
                                Map.of("message", "Transfer amount is required")),
                        new ValidationInfo(
                                "DecimalMin",
                                Map.of(
                                        "value", "0.01",
                                        "message", "must be greater than zero")),
                        new ValidationInfo(
                                "Digits",
                                Map.of("integer", "10", "fraction", "2")));
        assertThat(field(schema, "name").validations())
                .containsExactly(
                        new ValidationInfo(
                                "Size",
                                Map.of("min", "3", "max", "100")));
        assertThat(field(schema, "taxId").validations())
                .containsExactly(
                        new ValidationInfo(
                                "Pattern",
                                Map.of("regexp", "\\d{11}|\\d{14}")));
        assertThat(field(schema, "age").validations())
                .containsExactly(
                        new ValidationInfo("Min", Map.of("value", "18")));
    }

    @Test
    @DisplayName("Should extract schemas for nested class fields")
    void shouldExtractNestedSchemas() {
        CompilationUnit responseDto = parse("""
                package com.example.dto;

                import java.util.List;
                import java.util.Map;

                public record TransferResponseDTO(
                        List<SideEffectResult> sideEffects,
                        Address address,
                        List<Map<String, Detail>> details) {
                }
                """);

        CompilationUnit sideEffectDto = parse("""
                package com.example.dto;

                public record SideEffectResult(
                        String operation,
                        String status,
                        String message) {
                }
                """);

        CompilationUnit addressDto = parse("""
                package com.example.dto;

                public record Address(String street, String city) {
                }
                """);

        CompilationUnit detailDto = parse("""
                package com.example.dto;

                public record Detail(String code) {
                }
                """);

        TypeIndex typeIndex = TypeIndex.from(
                List.of(responseDto, sideEffectDto, addressDto, detailDto));

        List<SchemaInfo> schemas = dtoExtractor.extract(
                List.of("TransferResponseDTO"), typeIndex);

        assertThat(schemas)
                .extracting(SchemaInfo::name)
                .containsExactly(
                        "TransferResponseDTO",
                        "SideEffectResult",
                        "Address",
                        "Detail");
        assertThat(schema(schemas, "SideEffectResult").fields())
                .extracting(FieldInfo::name)
                .containsExactly("operation", "status", "message");
        assertThat(schema(schemas, "Address").fields())
                .extracting(FieldInfo::name)
                .containsExactly("street", "city");
        assertThat(schema(schemas, "Detail").fields())
                .extracting(FieldInfo::name)
                .containsExactly("code");
    }

    @Test
    @DisplayName("Should stop recursive extraction on cyclic DTO references")
    void shouldHandleCyclicReferences() {
        CompilationUnit parentDto = parse("""
                package com.example.dto;

                public record Parent(Child child) {
                }
                """);

        CompilationUnit childDto = parse("""
                package com.example.dto;

                public record Child(Parent parent, String name) {
                }
                """);

        TypeIndex typeIndex = TypeIndex.from(List.of(parentDto, childDto));

        List<SchemaInfo> schemas = dtoExtractor.extract(
                List.of("Parent"), typeIndex);

        assertThat(schemas)
                .extracting(SchemaInfo::name)
                .containsExactly("Parent", "Child");
        assertThat(schema(schemas, "Child").fields())
                .extracting(FieldInfo::name)
                .containsExactly("parent", "name");
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

    private SchemaInfo schema(List<SchemaInfo> schemas, String name) {
        return schemas.stream()
                .filter(schema -> schema.name().equals(name))
                .findFirst()
                .orElseThrow();
    }
}
