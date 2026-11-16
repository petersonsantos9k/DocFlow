package com.docflow.extraction.extract;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class DtoTypeResolver {

    private static final Set<String> WRAPPER_TYPES = Set.of(
            "ResponseEntity",
            "HttpEntity",
            "List",
            "Set",
            "Collection",
            "Iterable",
            "Optional",
            "Page",
            "Map",
            "Mono",
            "Flux",
            "CompletableFuture",
            "DeferredResult",
            "Callable"
    );

    private final TypeIndex typeIndex;

    public DtoTypeResolver(TypeIndex typeIndex) {
        this.typeIndex = typeIndex;
    }

    public List<String> resolve(MethodDeclaration method, CompilationUnit context) {
        LinkedHashSet<String> dtoNames = new LinkedHashSet<>();

        method.getParameters().stream()
                .filter(parameter -> parameter.isAnnotationPresent("RequestBody"))
                .forEach(parameter -> collect(parameter.getType(), context, dtoNames));

        collect(method.getType(), context, dtoNames);

        return List.copyOf(dtoNames);
    }

    private void collect(
            Type type,
            CompilationUnit context,
            Set<String> dtoNames) {
        if (type.isClassOrInterfaceType()) {
            collect(type.asClassOrInterfaceType(), context, dtoNames);
            return;
        }

        if (type.isArrayType()) {
            collect(type.asArrayType().getComponentType(), context, dtoNames);
            return;
        }

        if (type.isWildcardType()) {
            type.asWildcardType().getExtendedType()
                    .ifPresent(extended -> collect(extended, context, dtoNames));
            type.asWildcardType().getSuperType()
                    .ifPresent(superType -> collect(superType, context, dtoNames));
        }
    }

    private void collect(
            ClassOrInterfaceType type,
            CompilationUnit context,
            Set<String> dtoNames) {
        String typeName = type.getNameAsString();

        if (WRAPPER_TYPES.contains(typeName)) {
            type.getTypeArguments()
                    .ifPresent(arguments -> arguments.forEach(argument ->
                            collect(argument, context, dtoNames)));
            return;
        }

        typeIndex.resolve(type, context)
                .ifPresent(resolved -> dtoNames.add(resolved.getNameAsString()));

        type.getTypeArguments()
                .ifPresent(arguments -> arguments.forEach(argument ->
                        collect(argument, context, dtoNames)));
    }
}
