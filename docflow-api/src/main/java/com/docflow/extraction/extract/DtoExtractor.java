package com.docflow.extraction.extract;

import com.docflow.extraction.domain.model.FieldInfo;
import com.docflow.extraction.domain.model.SchemaInfo;
import com.docflow.extraction.domain.model.ValidationInfo;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DtoExtractor {

    private static final String VALUE = "value";

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
        Set<String> visitedTypes = new HashSet<>();
        Deque<TypeDeclaration<?>> pendingTypes = new ArrayDeque<>();

        dtoNames.stream()
                .map(typeIndex::resolveSimpleName)
                .flatMap(Optional::stream)
                .forEach(pendingTypes::add);

        while (!pendingTypes.isEmpty()) {
            TypeDeclaration<?> type = pendingTypes.removeFirst();
            String typeKey = type.getFullyQualifiedName()
                    .orElse(type.getNameAsString());

            if (!visitedTypes.add(typeKey)) {
                continue;
            }

            schemas.putIfAbsent(
                    type.getNameAsString(),
                    toSchema(type, typeIndex, pendingTypes));
        }

        return List.copyOf(schemas.values());
    }

    private SchemaInfo toSchema(
            TypeDeclaration<?> type,
            TypeIndex typeIndex,
            Deque<TypeDeclaration<?>> pendingTypes) {
        Set<FieldInfo> fields = new LinkedHashSet<>();
        CompilationUnit context = type.findCompilationUnit().orElse(null);

        if (type instanceof ClassOrInterfaceDeclaration classDeclaration) {
            for (FieldDeclaration field : classDeclaration.getFields()) {
                if (field.isStatic()) {
                    continue;
                }

                for (VariableDeclarator variable : field.getVariables()) {
                    addField(
                            fields,
                            variable.getNameAsString(),
                            variable.getType(),
                            field.getAnnotations(),
                            context,
                            typeIndex,
                            pendingTypes);
                }
            }
        } else if (type instanceof RecordDeclaration recordDeclaration) {
            for (Parameter parameter : recordDeclaration.getParameters()) {
                addField(
                        fields,
                        parameter.getNameAsString(),
                        parameter.getType(),
                        parameter.getAnnotations(),
                        context,
                        typeIndex,
                        pendingTypes);
            }
        }

        return new SchemaInfo(type.getNameAsString(), "DTO", fields);
    }

    private void addField(
            Set<FieldInfo> fields,
            String name,
            Type type,
            NodeList<AnnotationExpr> annotations,
            CompilationUnit context,
            TypeIndex typeIndex,
            Deque<TypeDeclaration<?>> pendingTypes) {
        fields.add(new FieldInfo(name, type.asString(), validations(annotations)));
        collectReferencedTypes(type, context, typeIndex, pendingTypes);
    }

    private List<ValidationInfo> validations(NodeList<AnnotationExpr> annotations) {
        return annotations.stream()
                .map(this::toValidationInfo)
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<ValidationInfo> toValidationInfo(AnnotationExpr annotation) {
        String name = simpleName(annotation.getNameAsString());

        if (!VALIDATION_ANNOTATIONS.contains(name)) {
            return Optional.empty();
        }

        Map<String, String> attributes = new LinkedHashMap<>();

        if (annotation.isSingleMemberAnnotationExpr()) {
            attributes.put(
                    VALUE,
                    readArgument(
                            annotation.asSingleMemberAnnotationExpr().getMemberValue()));
        } else if (annotation.isNormalAnnotationExpr()) {
            for (MemberValuePair pair : annotation.asNormalAnnotationExpr().getPairs()) {
                attributes.put(pair.getNameAsString(), readArgument(pair.getValue()));
            }
        }

        return Optional.of(new ValidationInfo(name, attributes));
    }

    private String readArgument(Expression expression) {
        if (expression.isStringLiteralExpr()) {
            return expression.asStringLiteralExpr().asString();
        }

        if (expression.isCharLiteralExpr()) {
            return String.valueOf(expression.asCharLiteralExpr().asChar());
        }

        if (expression.isBooleanLiteralExpr()) {
            return String.valueOf(expression.asBooleanLiteralExpr().getValue());
        }

        if (expression.isIntegerLiteralExpr()) {
            return expression.asIntegerLiteralExpr().getValue();
        }

        if (expression.isLongLiteralExpr()) {
            return expression.asLongLiteralExpr().getValue();
        }

        if (expression.isDoubleLiteralExpr()) {
            return expression.asDoubleLiteralExpr().getValue();
        }

        if (expression.isNameExpr()) {
            return expression.asNameExpr().getNameAsString();
        }

        if (expression.isArrayInitializerExpr()) {
            return expression.asArrayInitializerExpr().getValues().stream()
                    .map(this::readArgument)
                    .collect(Collectors.joining(", ", "[", "]"));
        }

        return expression.toString();
    }

    private void collectReferencedTypes(
            Type type,
            CompilationUnit context,
            TypeIndex typeIndex,
            Deque<TypeDeclaration<?>> pendingTypes) {
        if (type.isClassOrInterfaceType()) {
            collectReferencedTypes(
                    type.asClassOrInterfaceType(), context, typeIndex, pendingTypes);
            return;
        }

        if (type.isArrayType()) {
            collectReferencedTypes(
                    type.asArrayType().getComponentType(), context, typeIndex, pendingTypes);
            return;
        }

        if (type.isWildcardType()) {
            type.asWildcardType().getExtendedType().ifPresent(extended ->
                    collectReferencedTypes(
                            extended, context, typeIndex, pendingTypes));
            type.asWildcardType().getSuperType().ifPresent(superType ->
                    collectReferencedTypes(
                            superType, context, typeIndex, pendingTypes));
        }
    }

    private void collectReferencedTypes(
            ClassOrInterfaceType type,
            CompilationUnit context,
            TypeIndex typeIndex,
            Deque<TypeDeclaration<?>> pendingTypes) {
        if (context != null) {
            typeIndex.resolve(type, context)
                    .filter(this::isSchemaType)
                    .ifPresent(pendingTypes::add);
        }

        type.getTypeArguments().ifPresent(arguments ->
                arguments.forEach(argument ->
                        collectReferencedTypes(
                                argument, context, typeIndex, pendingTypes)));
    }

    private boolean isSchemaType(TypeDeclaration<?> type) {
        return type instanceof ClassOrInterfaceDeclaration
                || type instanceof RecordDeclaration;
    }

    private String simpleName(String typeName) {
        int lastDot = typeName.lastIndexOf('.');
        return lastDot < 0 ? typeName : typeName.substring(lastDot + 1);
    }
}
