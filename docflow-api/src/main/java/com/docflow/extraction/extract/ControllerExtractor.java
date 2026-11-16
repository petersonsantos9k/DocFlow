package com.docflow.extraction.extract;

import com.docflow.extraction.domain.exception.ExtractionException;
import com.docflow.extraction.domain.model.ControllerInfo;
import com.docflow.extraction.domain.model.EndpointInfo;
import com.docflow.extraction.domain.model.ParameterInfo;
import com.docflow.extraction.domain.model.enums.ParameterLocation;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class ControllerExtractor {

    private static final String REQUEST_MAPPING = "RequestMapping";
    private static final String PATH = "path";
    private static final String VALUE = "value";
    private static final String RESPONSE_STATUS = "ResponseStatus";
    private static final String PATH_VARIABLE = "PathVariable";
    private static final String REQUEST_PARAM = "RequestParam";
    private static final String REQUEST_HEADER = "RequestHeader";
    private static final String REQUEST_BODY = "RequestBody";
    private static final String REQUIRED = "required";
    private static final String DEFAULT_VALUE = "defaultValue";
    private static final Map<String, String> HTTP_METHODS = Map.of(
            "GetMapping", "GET",
            "PostMapping", "POST",
            "PutMapping", "PUT",
            "PatchMapping", "PATCH",
            "DeleteMapping", "DELETE");

    public ControllerInfo extract(CompilationUnit cu, TypeIndex typeIndex) {
        TypeDeclaration<?> type = cu.getPrimaryType()
                .orElseThrow(() -> new ExtractionException("Arquivo sem tipo principal"));

        String packageName = cu.getPackageDeclaration()
                .map(NodeWithName::getNameAsString)
                .orElse("");

        String basePath = type.getAnnotationByName(REQUEST_MAPPING)
                .map(this::readPath)
                .orElse("");

        DtoTypeResolver dtoTypeResolver = new DtoTypeResolver(typeIndex);

        List<EndpointInfo> endpoints = type.getMethods().stream()
                .map(method -> extractEndpoint(method, basePath, cu, dtoTypeResolver))
                .flatMap(Optional::stream)
                .toList();

        return new ControllerInfo(type.getNameAsString(), packageName, basePath, endpoints);
    }

    private Optional<EndpointInfo> extractEndpoint(
            MethodDeclaration method,
            String basePath,
            CompilationUnit cu,
            DtoTypeResolver dtoTypeResolver) {
        return readMapping(method)
                .map(mapping -> new EndpointInfo(
                        method.getNameAsString(),
                        mapping.method(),
                        joinPaths(basePath, mapping.path()),
                        extractParameters(method),
                        method.getType().asString(),
                        readResponseStatus(method),
                        dtoTypeResolver.resolve(method, cu)));
    }

    private Optional<Mapping> readMapping(MethodDeclaration method) {
        return method.getAnnotations().stream()
                .filter(annotation -> HTTP_METHODS.containsKey(
                        annotation.getName().getIdentifier()))
                .findFirst()
                .map(annotation -> new Mapping(
                        HTTP_METHODS.get(annotation.getName().getIdentifier()),
                        readPath(annotation)));
    }

    private String readPath(AnnotationExpr annotation) {
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

    private List<ParameterInfo> extractParameters(MethodDeclaration method) {
        return method.getParameters().stream()
                .map(this::toParameterInfo)
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<ParameterInfo> toParameterInfo(Parameter parameter) {
        ParameterLocation location = parameterLocation(parameter);
        if (location == null) {
            return Optional.empty();
        }

        return Optional.of(new ParameterInfo(
                parameter.getNameAsString(),
                location,
                parameter.getType().asString(),
                isRequired(parameter, location)));
    }

    private ParameterLocation parameterLocation(Parameter parameter) {
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

    private boolean isRequired(Parameter parameter, ParameterLocation location) {
        String annotationName = switch (location) {
            case PATH -> PATH_VARIABLE;
            case QUERY -> REQUEST_PARAM;
            case HEADER -> REQUEST_HEADER;
            case BODY -> REQUEST_BODY;
        };

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

    private Integer readResponseStatus(MethodDeclaration method) {
        return method.getAnnotationByName(RESPONSE_STATUS)
                .filter(AnnotationExpr::isSingleMemberAnnotationExpr)
                .map(annotation -> annotation.asSingleMemberAnnotationExpr().getMemberValue())
                .filter(Expression::isFieldAccessExpr)
                .map(expression -> HttpStatus.valueOf(
                        expression.asFieldAccessExpr().getNameAsString()).value())
                .orElse(null);
    }

    private String joinPaths(String base, String path) {
        return ("/" + base + "/" + path).replaceAll("/+", "/")
                .replaceAll("(?<=.)/$", "");
    }

    private record Mapping(String method, String path) {
    }
}
