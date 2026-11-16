package com.docflow.extraction.extract;

import com.docflow.extraction.domain.model.FieldInfo;
import com.docflow.extraction.domain.model.SchemaInfo;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class DtoExtractor {

    private static final Set<String> VALIDATION_ANNOTATIONS = Set.of(
            "AssertFalse",
            "AssertTrue",
            "DecimalMax",
            "DecimalMin",
            "Digits",
            "Email",
            "Future",
            "FutureOrPresent",
            "Max",
            "Min",
            "Negative",
            "NegativeOrZero",
            "NotBlank",
            "NotEmpty",
            "NotNull",
            "Past",
            "PastOrPresent",
            "Pattern",
            "Positive",
            "PositiveOrZero",
            "Size",
            "Valid"
    );

    public List<SchemaInfo> extract(
            Collection<String> dtoNames,
            TypeIndex typeIndex) {
        Map<String, SchemaInfo> schemas = new LinkedHashMap<>();

        for (String dtoName : dtoNames) {
            typeIndex.resolveSimpleName(dtoName)
                    .map(this::toSchema)
                    .ifPresent(schema -> schemas.putIfAbsent(schema.name(), schema));
        }

        return List.copyOf(schemas.values());
    }

    private SchemaInfo toSchema(TypeDeclaration<?> type) {
        Set<FieldInfo> fields = new LinkedHashSet<>();

        if (type instanceof ClassOrInterfaceDeclaration classDeclaration) {
            for (FieldDeclaration field : classDeclaration.getFields()) {
                if (field.isStatic()) {
                    continue;
                }

                for (VariableDeclarator variable : field.getVariables()) {
                    fields.add(new FieldInfo(
                            variable.getNameAsString(),
                            variable.getType().asString(),
                            validations(field.getAnnotations())));
                }
            }
        } else if (type instanceof RecordDeclaration recordDeclaration) {
            for (Parameter parameter : recordDeclaration.getParameters()) {
                fields.add(new FieldInfo(
                        parameter.getNameAsString(),
                        parameter.getType().asString(),
                        validations(parameter.getAnnotations())));
            }
        }

        return new SchemaInfo(type.getNameAsString(), "DTO", fields);
    }

    private List<String> validations(NodeList<AnnotationExpr> annotations) {
        return annotations.stream()
                .map(AnnotationExpr::getNameAsString)
                .filter(VALIDATION_ANNOTATIONS::contains)
                .toList();
    }
}
