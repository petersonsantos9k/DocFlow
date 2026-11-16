package com.docflow.extraction.extract;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class TypeIndex {

    private final Map<String, List<TypeDeclaration<?>>> bySimpleName = new HashMap<>();
    private final Map<String, TypeDeclaration<?>> byQualifiedName = new HashMap<>();

    private TypeIndex() {
    }

    public static TypeIndex from(Collection<CompilationUnit> compilationUnits) {
        TypeIndex index = new TypeIndex();

        for (CompilationUnit compilationUnit : compilationUnits) {
            compilationUnit.findAll(TypeDeclaration.class).forEach(rawType -> {
                TypeDeclaration<?> type = rawType;
                index.bySimpleName
                        .computeIfAbsent(type.getNameAsString(), ignored -> new ArrayList<>())
                        .add(type);

                type.getFullyQualifiedName().ifPresent(qualifiedName ->
                        index.byQualifiedName.put(qualifiedName, type));
            });
        }

        return index;
    }

    public Optional<TypeDeclaration<?>> resolve(
            ClassOrInterfaceType type,
            CompilationUnit context) {
        Optional<TypeDeclaration<?>> qualified = resolve(type.getNameWithScope(), context);
        if (qualified.isPresent()) {
            return qualified;
        }
        return resolve(type.getNameAsString(), context);
    }

    public Optional<TypeDeclaration<?>> resolve(
            String typeName,
            CompilationUnit context) {
        TypeDeclaration<?> exact = byQualifiedName.get(typeName);
        if (exact != null) {
            return Optional.of(exact);
        }

        String simpleName = simpleName(typeName);

        Optional<TypeDeclaration<?>> imported = resolveFromImports(simpleName, context);
        if (imported.isPresent()) {
            return imported;
        }

        Optional<TypeDeclaration<?>> samePackage = resolveFromPackage(simpleName, context);
        if (samePackage.isPresent()) {
            return samePackage;
        }

        return resolveSimpleName(simpleName);
    }

    public Optional<TypeDeclaration<?>> resolveSimpleName(String typeName) {
        TypeDeclaration<?> exact = byQualifiedName.get(typeName);
        if (exact != null) {
            return Optional.of(exact);
        }

        List<TypeDeclaration<?>> candidates =
                bySimpleName.getOrDefault(simpleName(typeName), List.of());

        return candidates.size() == 1
                ? Optional.of(candidates.get(0))
                : Optional.empty();
    }

    public Optional<TypeDeclaration<?>> resolve(Type type, CompilationUnit context) {
        if (!type.isClassOrInterfaceType()) {
            return Optional.empty();
        }
        return resolve(type.asClassOrInterfaceType(), context);
    }

    private Optional<TypeDeclaration<?>> resolveFromImports(
            String simpleName,
            CompilationUnit context) {
        for (ImportDeclaration importDeclaration : context.getImports()) {
            String importedName = importDeclaration.getNameAsString();

            if (importDeclaration.isAsterisk()) {
                TypeDeclaration<?> type = byQualifiedName.get(importedName + "." + simpleName);
                if (type != null) {
                    return Optional.of(type);
                }
                continue;
            }

            if (simpleName(importedName).equals(simpleName)) {
                TypeDeclaration<?> type = byQualifiedName.get(importedName);
                if (type != null) {
                    return Optional.of(type);
                }
            }
        }

        return Optional.empty();
    }

    private Optional<TypeDeclaration<?>> resolveFromPackage(
            String simpleName,
            CompilationUnit context) {
        String packageName = context.getPackageDeclaration()
                .map(packageDeclaration -> packageDeclaration.getNameAsString())
                .orElse("");

        String qualifiedName = packageName.isEmpty()
                ? simpleName
                : packageName + "." + simpleName;

        return Optional.ofNullable(byQualifiedName.get(qualifiedName));
    }

    private String simpleName(String typeName) {
        int lastDot = typeName.lastIndexOf('.');
        return lastDot < 0 ? typeName : typeName.substring(lastDot + 1);
    }
}
