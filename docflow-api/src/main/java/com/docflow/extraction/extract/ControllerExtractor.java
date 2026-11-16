package com.docflow.extraction.extract;

import com.docflow.extraction.domain.exception.ExtractionException;
import com.docflow.extraction.domain.model.ControllerInfo;
import com.docflow.extraction.domain.model.EndpointInfo;
import com.docflow.extraction.domain.model.ParameterInfo;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ControllerExtractor {

    private static final String REQUEST_MAPPING = "RequestMapping";

    private final EndpointMappingReader mappingReader = new EndpointMappingReader();
    private final ParameterExtractor parameterExtractor = new ParameterExtractor();
    private final ResponseStatusReader responseStatusReader = new ResponseStatusReader();

    public ControllerInfo extract(CompilationUnit cu, TypeIndex typeIndex) {
        TypeDeclaration<?> type = cu.getPrimaryType()
                .orElseThrow(() -> new ExtractionException("Arquivo sem tipo principal"));

        String packageName = cu.getPackageDeclaration()
                .map(NodeWithName::getNameAsString)
                .orElse("");

        String basePath = type.getAnnotationByName(REQUEST_MAPPING)
                .map(mappingReader::readPath)
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
        return mappingReader.read(method)
                .map(mapping -> new EndpointInfo(
                        method.getNameAsString(),
                        mapping.httpMethod(),
                        joinPaths(basePath, mapping.path()),
                        parameterExtractor.extract(method),
                        method.getType().asString(),
                        responseStatusReader.read(method),
                        dtoTypeResolver.resolve(method, cu)));
    }

    private String joinPaths(String base, String path) {
        return ("/" + base + "/" + path).replaceAll("/+", "/")
                .replaceAll("(?<=.)/$", "");
    }
}
