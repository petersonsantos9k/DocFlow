package com.docflow.extraction.orchestrator;

import com.docflow.extraction.classifier.JavaClassClassifier;
import com.docflow.extraction.domain.exception.AnalyzerException;
import com.docflow.extraction.domain.model.ControllerInfo;
import com.docflow.extraction.domain.model.ProjectInfo;
import com.docflow.extraction.domain.model.ProjectInfoSpringBoot;
import com.docflow.extraction.domain.model.ProjectModel;
import com.docflow.extraction.domain.model.SchemaInfo;
import com.docflow.extraction.domain.model.enums.JavaClassType;
import com.docflow.extraction.extract.ControllerExtractor;
import com.docflow.extraction.extract.DtoExtractor;
import com.docflow.extraction.extract.ProjectInfoExtractor;
import com.docflow.extraction.extract.TypeIndex;
import com.docflow.extraction.scanner.JavaSourceAnalyzer;
import com.docflow.extraction.scanner.JavaSourceFileScanner;

import com.github.javaparser.ast.CompilationUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExtractionOrchestrator {

    private final JavaSourceFileScanner sourceFileScanner;
    private final JavaSourceAnalyzer sourceAnalyzer;
    private final JavaClassClassifier classClassifier;
    private final ControllerExtractor controllerExtractor;
    private final DtoExtractor dtoExtractor;
    private final ProjectInfoExtractor projectInfoExtractor;

    public ProjectModel<ProjectInfoSpringBoot> extract(Path rootPath) {
        List<CompilationUnit> compilationUnits = parseCompilationUnits(rootPath);

        ProjectInfo<ProjectInfoSpringBoot> projectInfo =
                projectInfoExtractor.extract(rootPath);

        TypeIndex typeIndex = TypeIndex.from(compilationUnits);
        List<ControllerInfo> controllers = extractControllers(compilationUnits, typeIndex);
        Set<String> dtoNames = collectDtoNames(controllers);
        List<SchemaInfo> schemas = dtoExtractor.extract(dtoNames, typeIndex);

        return new ProjectModel<>(
                projectInfo,
                controllers,
                schemas
        );
    }

    private List<CompilationUnit> parseCompilationUnits(Path rootPath) {
        List<CompilationUnit> compilationUnits = new ArrayList<>();

        for (Path path : sourceFileScanner.findJavaFiles(rootPath)) {
            try {
                compilationUnits.add(sourceAnalyzer.analyze(path));
            } catch (AnalyzerException err) {
                log.warn("Skipping invalid Java source file: {}", path, err);
            }
        }

        return compilationUnits;
    }

    private List<ControllerInfo> extractControllers(
            List<CompilationUnit> compilationUnits,
            TypeIndex typeIndex) {
        List<ControllerInfo> controllers = new ArrayList<>();

        for (CompilationUnit compilationUnit : compilationUnits) {
            if (classClassifier.classifier(compilationUnit) == JavaClassType.CONTROLLER) {
                controllers.add(controllerExtractor.extract(compilationUnit, typeIndex));
            }
        }

        return controllers;
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
