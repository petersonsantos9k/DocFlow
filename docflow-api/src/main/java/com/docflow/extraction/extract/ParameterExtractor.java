package com.docflow.extraction.extract;

import com.docflow.extraction.domain.model.ParameterInfo;
import com.docflow.extraction.domain.model.enums.ParameterLocation;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;

import java.util.List;
import java.util.Optional;

final class ParameterExtractor {

    private static final String PATH_VARIABLE = "PathVariable";
    private static final String REQUEST_PARAM = "RequestParam";
    private static final String REQUEST_HEADER = "RequestHeader";
    private static final String REQUEST_BODY = "RequestBody";
    private static final String REQUIRED = "required";
    private static final String DEFAULT_VALUE = "defaultValue";

    List<ParameterInfo> extract(MethodDeclaration method) {
        return method.getParameters().stream()
                .map(this::toParameterInfo)
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<ParameterInfo> toParameterInfo(Parameter parameter) {
        ParameterLocation location = location(parameter);
        if (location == null) {
            return Optional.empty();
        }

        return Optional.of(new ParameterInfo(
                parameter.getNameAsString(),
                location,
                parameter.getType().asString(),
                isRequired(parameter, annotationName(location))));
    }

    private ParameterLocation location(Parameter parameter) {
        if (parameter.isAnnotationPresent(PATH_VARIABLE)) {
            return ParameterLocation.PATH;
        }
        if (parameter.isAnnotationPresent(REQUEST_PARAM)) {
            return ParameterLocation.QUERY;
        }
        if (parameter.isAnnotationPresent(REQUEST_HEADER)) {
            return ParameterLocation.HEADER;
        }
        if (parameter.isAnnotationPresent(REQUEST_BODY)) {
            return ParameterLocation.BODY;
        }
        return null;
    }

    private String annotationName(ParameterLocation location) {
        return switch (location) {
            case PATH -> PATH_VARIABLE;
            case QUERY -> REQUEST_PARAM;
            case HEADER -> REQUEST_HEADER;
            case BODY -> REQUEST_BODY;
        };
    }

    private boolean isRequired(Parameter parameter, String annotationName) {
        return parameter.getAnnotationByName(annotationName)
                .map(this::isRequired)
                .orElse(true);
    }

    private boolean isRequired(AnnotationExpr annotation) {
        if (!annotation.isNormalAnnotationExpr()) {
            return true;
        }

        for (MemberValuePair pair : annotation.asNormalAnnotationExpr().getPairs()) {
            if (pair.getNameAsString().equals(DEFAULT_VALUE)) {
                return false;
            }

            if (pair.getNameAsString().equals(REQUIRED)
                    && pair.getValue().isBooleanLiteralExpr()) {
                return pair.getValue().asBooleanLiteralExpr().getValue();
            }
        }

        return true;
    }
}
