package com.docflow.extraction.extract;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TypeIndexTest {

    @Test
    @DisplayName("Should resolve types by explicit import, wildcard, same package and qualified name")
    void shouldResolveTypesByPriority() {
        CompilationUnit explicitImportController = StaticJavaParser.parse("""
                package com.example.web;
                import com.example.dto.User;
                public class ExplicitImportController {
                    private User value;
                }
                """);

        CompilationUnit wildcardController = StaticJavaParser.parse("""
                package com.example.web;
                import com.other.*;
                public class WildcardController {
                    private User value;
                }
                """);

        CompilationUnit samePackageController = StaticJavaParser.parse("""
                package com.example.dto;
                public class SamePackageController {
                    private User value;
                }
                """);

        CompilationUnit ambiguousController = StaticJavaParser.parse("""
                package com.example.unknown;
                public class AmbiguousController {
                    private User value;
                }
                """);

        CompilationUnit user = StaticJavaParser.parse("""
                package com.example.dto;
                public class User {
                }
                """);

        CompilationUnit otherUser = StaticJavaParser.parse("""
                package com.other;
                public class User {
                }
                """);

        TypeIndex index = TypeIndex.from(List.of(
                explicitImportController,
                wildcardController,
                samePackageController,
                ambiguousController,
                user,
                otherUser));

        assertThat(index.resolve("User", explicitImportController).orElseThrow())
                .isSameAs(user.getType(0));
        assertThat(index.resolve("User", wildcardController).orElseThrow())
                .isSameAs(otherUser.getType(0));
        assertThat(index.resolve("User", samePackageController).orElseThrow())
                .isSameAs(user.getType(0));
        assertThat(index.resolve("com.other.User", ambiguousController).orElseThrow())
                .isSameAs(otherUser.getType(0));
        assertThat(index.resolve("User", ambiguousController)).isEmpty();
        assertThat(index.resolveSimpleName("User")).isEmpty();
    }

    @Test
    @DisplayName("Should resolve a ClassOrInterfaceType using the controller context")
    void shouldResolveClassOrInterfaceType() {
        CompilationUnit controller = StaticJavaParser.parse("""
                package com.example.web;
                import com.example.dto.User;
                public class UserController {
                    private User value;
                }
                """);

        CompilationUnit user = StaticJavaParser.parse("""
                package com.example.dto;
                public class User {
                }
                """);

        TypeIndex index = TypeIndex.from(List.of(controller, user));

        ClassOrInterfaceType type = controller.findAll(ClassOrInterfaceType.class).stream()
                .filter(candidate -> candidate.getNameAsString().equals("User"))
                .findFirst()
                .orElseThrow();

        assertThat(index.resolve(type, controller).orElseThrow())
                .isSameAs(user.getType(0));
    }
}
