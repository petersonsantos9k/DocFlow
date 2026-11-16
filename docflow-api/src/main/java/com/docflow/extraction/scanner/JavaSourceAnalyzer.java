package com.docflow.extraction.scanner;

import com.docflow.extraction.domain.exception.AnalyzerException;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Component
public class JavaSourceAnalyzer {
    private final JavaParser parser;

    public JavaSourceAnalyzer(){
        var configuration = new ParserConfiguration()
                .setLanguageLevel(
                        ParserConfiguration.LanguageLevel.JAVA_25
                );
        this.parser = new JavaParser(configuration);
    }

    public CompilationUnit analyze(Path filePath) {
        try {
            ParseResult<CompilationUnit> result = parser.parse(filePath);

            if (!result.isSuccessful() || result.getResult().isEmpty()) {
                throw new AnalyzerException(
                        "Failed to parse Java source file: " + filePath
                );
            }
            return result.getResult().get();
        } catch (IOException e) {
            throw new AnalyzerException(
                    "Failed to read Java source file: " + filePath, e
            );
        }
    }

    public List<CompilationUnit> analyzeProject(Path rootPath) {
        List<CompilationUnit> compilationUnits = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(rootPath)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            compilationUnits.add(analyze(path));
                        } catch (AnalyzerException err) {
                            log.warn("Skipping invalid Java source file: {}", path, err);
                        }
                    });
        } catch (IOException err) {
            throw new AnalyzerException("Failed to scan Java source files.", err);
        }

        return compilationUnits;
    }
}
