package com.docflow.extraction.extract;

import com.docflow.extraction.domain.model.ProjectInfo;
import com.docflow.extraction.domain.model.ProjectInfoSpringBoot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectInfoExtractorTest {

    @TempDir
    Path tempDir;

    private final ProjectInfoExtractor projectInfoExtractor = new ProjectInfoExtractor();

    @Test
    @DisplayName("Should extract Maven project metadata and Spring Boot dependencies")
    void shouldExtractMavenProjectInfo() throws IOException {
        Files.writeString(tempDir.resolve("pom.xml"), """
                <project>
                    <modelVersion>4.0.0</modelVersion>
                    <parent>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-parent</artifactId>
                        <version>3.3.0</version>
                    </parent>
                    <artifactId>user-api</artifactId>
                    <name>User API</name>
                    <properties>
                        <java.version>21</java.version>
                    </properties>
                    <dependencies>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-web</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-validation</artifactId>
                        </dependency>
                    </dependencies>
                </project>
                """);

        ProjectInfo<ProjectInfoSpringBoot> project =
                projectInfoExtractor.extract(tempDir);

        assertThat(project.name()).isEqualTo("User API");
        assertThat(project.buildTool()).isEqualTo("Maven");
        assertThat(project.javaVersion()).isEqualTo("21");
        assertThat(project.type().springBootVersion()).isEqualTo("3.3.0");
        assertThat(project.type().dependencies())
                .containsExactly("spring-boot-starter-web", "spring-boot-starter-validation");
    }

    @Test
    @DisplayName("Should detect Gradle projects without build metadata")
    void shouldDetectGradleProject() throws IOException {
        Files.writeString(tempDir.resolve("build.gradle"), "");

        ProjectInfo<ProjectInfoSpringBoot> project =
                projectInfoExtractor.extract(tempDir);

        assertThat(project.name()).isEqualTo(tempDir.getFileName().toString());
        assertThat(project.buildTool()).isEqualTo("Gradle");
        assertThat(project.javaVersion()).isNull();
        assertThat(project.type().springBootVersion()).isNull();
        assertThat(project.type().dependencies()).isEmpty();
    }

    @Test
    @DisplayName("Should return basic project info when no build file exists")
    void shouldReturnBasicProjectInfoWithoutBuildFile() {
        ProjectInfo<ProjectInfoSpringBoot> project =
                projectInfoExtractor.extract(tempDir);

        assertThat(project.name()).isEqualTo(tempDir.getFileName().toString());
        assertThat(project.buildTool()).isEqualTo("Unknown");
        assertThat(project.javaVersion()).isNull();
        assertThat(project.type()).isNotNull();
        assertThat(project.type().dependencies()).isEmpty();
    }
}
