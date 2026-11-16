package com.docflow.extraction.extract;

import com.docflow.extraction.domain.model.ProjectInfo;
import com.docflow.extraction.domain.model.ProjectInfoSpringBoot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
public class ProjectInfoExtractor {

    public ProjectInfo<ProjectInfoSpringBoot> extract(Path rootPath) {
        Path pom = rootPath.resolve("pom.xml");

        if (Files.isRegularFile(pom)) {
            try {
                return extractMaven(rootPath, pom);
            } catch (Exception err) {
                log.warn("Could not read Maven project metadata from '{}'.", pom, err);
                return basicProject(rootPath, "Maven");
            }
        }

        if (Files.isRegularFile(rootPath.resolve("build.gradle"))
                || Files.isRegularFile(rootPath.resolve("build.gradle.kts"))) {
            return basicProject(rootPath, "Gradle");
        }

        return basicProject(rootPath, "Unknown");
    }

    private ProjectInfo<ProjectInfoSpringBoot> extractMaven(
            Path rootPath,
            Path pom) throws Exception {
        Document document = parse(pom);
        Element project = document.getDocumentElement();

        String name = childText(project, "name")
                .or(() -> childText(project, "artifactId"))
                .orElseGet(() -> directoryName(rootPath));

        String springBootVersion = springBootVersion(project).orElse(null);

        ProjectInfoSpringBoot springBoot = new ProjectInfoSpringBoot(
                springBootVersion,
                dependencies(project));

        return new ProjectInfo<>(
                name,
                "Maven",
                javaVersion(project).orElse(null),
                springBoot);
    }

    private Document parse(Path pom) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(
                "http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature(
                "http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature(
                "http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        return factory.newDocumentBuilder().parse(pom.toFile());
    }

    private Optional<String> javaVersion(Element project) {
        return property(project, "java.version")
                .or(() -> property(project, "maven.compiler.release"))
                .or(() -> property(project, "maven.compiler.source"));
    }

    private Optional<String> springBootVersion(Element project) {
        Optional<Element> parent = child(project, "parent");
        if (parent.isPresent()) {
            String artifactId = childText(parent.get(), "artifactId").orElse("");
            if (artifactId.equals("spring-boot-starter-parent")) {
                return childText(parent.get(), "version");
            }
        }

        return child(project, "dependencyManagement")
                .flatMap(management -> child(management, "dependencies"))
                .map(dependencies -> directChildren(dependencies, "dependency").stream()
                        .filter(dependency -> "spring-boot-dependencies".equals(
                                childText(dependency, "artifactId").orElse("")))
                        .findFirst()
                        .flatMap(dependency -> childText(dependency, "version")))
                .orElse(Optional.empty());
    }

    private Set<String> dependencies(Element project) {
        Set<String> dependencies = new LinkedHashSet<>();

        child(project, "dependencies").ifPresent(dependenciesElement ->
                directChildren(dependenciesElement, "dependency").forEach(dependency ->
                        childText(dependency, "artifactId").ifPresent(dependencies::add)));

        return dependencies;
    }

    private Optional<String> property(Element project, String name) {
        return child(project, "properties")
                .flatMap(properties -> childText(properties, name));
    }

    private Optional<Element> child(Element parent, String tagName) {
        for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof Element element && element.getTagName().equals(tagName)) {
                return Optional.of(element);
            }
        }
        return Optional.empty();
    }

    private Optional<String> childText(Element parent, String tagName) {
        return child(parent, tagName)
                .map(Element::getTextContent)
                .map(String::trim)
                .filter(value -> !value.isEmpty());
    }

    private List<Element> directChildren(Element parent, String tagName) {
        List<Element> children = new ArrayList<>();

        for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof Element element && element.getTagName().equals(tagName)) {
                children.add(element);
            }
        }

        return children;
    }

    private ProjectInfo<ProjectInfoSpringBoot> basicProject(
            Path rootPath,
            String buildTool) {
        return new ProjectInfo<>(
                directoryName(rootPath),
                buildTool,
                null,
                new ProjectInfoSpringBoot(null, Set.of()));
    }

    private String directoryName(Path rootPath) {
        Path fileName = rootPath.getFileName();
        return fileName == null ? "unknown" : fileName.toString();
    }
}
