package com.docflow.extraction.orchestrator;

import com.docflow.extraction.domain.model.ControllerInfo;
import com.docflow.extraction.domain.model.ProjectInfo;
import com.docflow.extraction.domain.model.ProjectModel;
import com.docflow.extraction.domain.model.SchemaInfo;
import com.docflow.extraction.extract.ControllerExtractor;
import com.docflow.extraction.extract.DtoExtractor;
import com.docflow.extraction.extract.ProjectInfoExtractor;
import com.docflow.extraction.extract.TypeIndex;
import com.docflow.extraction.scanner.JavaSourceAnalyzer;

import com.github.javaparser.ast.CompilationUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ExtractionOrchestrator {

    private final JavaSourceAnalyzer sourceAnalyzer;
    private final ControllerExtractor controllerExtractor;
    private final DtoExtractor dtoExtractor;
    private final ProjectInfoExtractor projectInfoExtractor;

    public ProjectModel extract(Path rootPath) {
        List<CompilationUnit> compilationUnits = sourceAnalyzer.analyzeProject(rootPath);

        ProjectInfo projectInfo = projectInfoExtractor.extract(rootPath);

        TypeIndex typeIndex = TypeIndex.from(compilationUnits);
        List<ControllerInfo> controllers = extractControllers(compilationUnits, typeIndex);
        Set<String> dtoNames = collectDtoNames(controllers);
        List<SchemaInfo> schemas = dtoExtractor.extract(dtoNames, typeIndex);

        return new ProjectModel(
                projectInfo,
                controllers,
                schemas
        );
    }

    private List<ControllerInfo> extractControllers(
            List<CompilationUnit> compilationUnits,
            TypeIndex typeIndex) {
        return compilationUnits.stream()
                .filter(this::isController)
                .map(compilationUnit -> controllerExtractor.extract(compilationUnit, typeIndex))
                .toList();
    }

    private boolean isController(CompilationUnit compilationUnit) {
        return compilationUnit.getTypes().stream()
                .anyMatch(type -> type.isAnnotationPresent("RestController")
                        || type.isAnnotationPresent("Controller"));
    }

    private Set<String> collectDtoNames(List<ControllerInfo> controllers) {
        Set<String> dtoNames = new LinkedHashSet<>();

        for (ControllerInfo controller : controllers) {
            controller.endpoints().stream()
                    .flatMap(endpoint -> endpoint.dtos().stream())
                    .forEach(dtoNames::add);
        }

        return dtoNames;
    }
}
