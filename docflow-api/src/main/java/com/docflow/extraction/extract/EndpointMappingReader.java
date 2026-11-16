package com.docflow.extraction.extract;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;

import java.util.Map;
import java.util.Optional;

final class EndpointMappingReader {

    private static final String PATH = "path";
    private static final String VALUE = "value";

    private static final Map<String, String> VERBS = Map.of(
            "GetMapping", "GET",
            "PostMapping", "POST",
            "PutMapping", "PUT",
            "PatchMapping", "PATCH",
            "DeleteMapping", "DELETE");

    Optional<Mapping> read(MethodDeclaration method) {
        return method.getAnnotations().stream()
                .filter(annotation -> VERBS.containsKey(annotation.getName().getIdentifier()))
                .findFirst()
                .map(annotation -> new Mapping(
                        VERBS.get(annotation.getName().getIdentifier()),
                        readPath(annotation)));
    }

    String readPath(AnnotationExpr annotation) {
        if (annotation.isSingleMemberAnnotationExpr()) {
            return readPathValue(annotation.asSingleMemberAnnotationExpr().getMemberValue());
        }

        if (annotation.isNormalAnnotationExpr()) {
            for (MemberValuePair pair : annotation.asNormalAnnotationExpr().getPairs()) {
                if (pair.getNameAsString().equals(VALUE)
                        || pair.getNameAsString().equals(PATH)) {
                    return readPathValue(pair.getValue());
                }
            }
        }

        return "";
    }

    private String readPathValue(Expression expression) {
        if (expression.isStringLiteralExpr()) {
            return expression.asStringLiteralExpr().asString();
        }

        if (expression.isArrayInitializerExpr()) {
            return expression.asArrayInitializerExpr().getValues().stream()
                    .findFirst()
                    .map(this::readPathValue)
                    .orElse("");
        }

        return "";
    }

    record Mapping(String httpMethod, String path) {
    }
}
