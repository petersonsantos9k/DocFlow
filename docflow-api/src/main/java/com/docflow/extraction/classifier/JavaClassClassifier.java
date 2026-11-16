package com.docflow.extraction.classifier;

import com.docflow.extraction.domain.model.enums.JavaClassType;
import com.github.javaparser.ast.CompilationUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JavaClassClassifier {

    private static final String[] CONTROLLER_ANNOTATIONS = {"RestController", "Controller"};

    private final AnnotationClassifier annotationClassifier;

    public JavaClassType classifier(CompilationUnit compilationUnit){
        if(annotationClassifier.matchesClassAnnotation(
                compilationUnit, CONTROLLER_ANNOTATIONS )){
            return JavaClassType.CONTROLLER;
        }
        return JavaClassType.UNKNOWN;
    }

}
