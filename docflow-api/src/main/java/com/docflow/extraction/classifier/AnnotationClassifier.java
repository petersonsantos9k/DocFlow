package com.docflow.extraction.classifier;

import com.github.javaparser.ast.CompilationUnit;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class AnnotationClassifier {

    public boolean matchesClassAnnotation(
            CompilationUnit compilationUnit,
            String... annotationNames) {

        return compilationUnit.getTypes().stream()
                .anyMatch(type ->
                        Arrays.stream(annotationNames)
                                .anyMatch(type::isAnnotationPresent)
                );
    }

}
