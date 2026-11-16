package com.docflow.extraction.extract;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import org.springframework.http.HttpStatus;

final class ResponseStatusReader {

    private static final String RESPONSE_STATUS = "ResponseStatus";

    Integer read(MethodDeclaration method) {
        return method.getAnnotationByName(RESPONSE_STATUS)
                .filter(AnnotationExpr::isSingleMemberAnnotationExpr)
                .map(annotation -> annotation.asSingleMemberAnnotationExpr().getMemberValue())
                .filter(Expression::isFieldAccessExpr)
                .map(expression -> HttpStatus.valueOf(
                        expression.asFieldAccessExpr().getNameAsString()).value())
                .orElse(null);
    }
}
