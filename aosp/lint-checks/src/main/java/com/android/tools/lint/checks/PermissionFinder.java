/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.tools.lint.checks;

import static com.android.SdkConstants.CLASS_INTENT;
import static com.android.tools.lint.checks.SupportAnnotationDetector.PERMISSION_ANNOTATION;
import static com.android.tools.lint.checks.SupportAnnotationDetector.PERMISSION_ANNOTATION_READ;
import static com.android.tools.lint.checks.SupportAnnotationDetector.PERMISSION_ANNOTATION_WRITE;
import static com.android.tools.lint.detector.api.JavaContext.getParentOfType;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.tools.lint.client.api.JavaParser.ResolvedAnnotation;
import com.android.tools.lint.client.api.JavaParser.ResolvedField;
import com.android.tools.lint.client.api.JavaParser.ResolvedNode;
import com.android.tools.lint.detector.api.JavaContext;

import java.util.ListIterator;

import lombok.ast.BinaryExpression;
import lombok.ast.BinaryOperator;
import lombok.ast.Cast;
import lombok.ast.ConstructorInvocation;
import lombok.ast.Expression;
import lombok.ast.ExpressionStatement;
import lombok.ast.InlineIfExpression;
import lombok.ast.Node;
import lombok.ast.NullLiteral;
import lombok.ast.Select;
import lombok.ast.Statement;
import lombok.ast.VariableDeclaration;
import lombok.ast.VariableDefinition;
import lombok.ast.VariableDefinitionEntry;
import lombok.ast.VariableReference;

/**
 * Utility for locating permissions required by an intent or content resolver
 */
public class PermissionFinder {
    /**
     * Operation that has a permission requirement -- such as a method call,
     * a content resolver read or write operation, an intent, etc.
     */
    public enum Operation {
        CALL, ACTION, READ, WRITE;

        /** Prefix to use when describing a name with a permission requirement */
        public String prefix() {
            switch (this) {
                case ACTION:
                    return "by intent";
                case READ:
                    return "to read";
                case WRITE:
                    return "to write";
                case CALL:
                default:
                    return "by";
            }
        }
    }

    /** A permission requirement given a name and operation */
    public static class Result {
        @NonNull public final PermissionRequirement requirement;
        @NonNull public final String name;
        @NonNull public final Operation operation;

        public Result(
                @NonNull Operation operation,
                @NonNull PermissionRequirement requirement,
                @NonNull String name) {
            this.operation = operation;
            this.requirement = requirement;
            this.name = name;
        }
    }

    /**
     * Searches for a permission requirement for the given parameter in the given call
     *
     * @param operation the operation to look up
     * @param context   the context to use for lookup
     * @param parameter the parameter which contains the value which implies the permission
     * @return the result with the permission requirement, or null if nothing is found
     */
    @Nullable
    public static Result findRequiredPermissions(
            @NonNull Operation operation,
            @NonNull JavaContext context,
            @NonNull Node parameter) {

        // To find the permission required by an intent, we proceed in 3 steps:
        // (1) Locate the parameter in the start call that corresponds to
        //     the Intent
        //
        // (2) Find the place where the intent is initialized, and figure
        //     out the action name being passed to it.
        //
        // (3) Find the place where the action is defined, and look for permission
        //     annotations on that action declaration!

        return new PermissionFinder(context, operation).search(parameter);
    }

    private PermissionFinder(@NonNull JavaContext context, @NonNull Operation operation) {
        mContext = context;
        mOperation = operation;
    }

    @NonNull private final JavaContext mContext;
    @NonNull private final Operation mOperation;

    @Nullable
    public Result search(@NonNull Node node) {
        if (node instanceof NullLiteral) {
            return null;
        } else if (node instanceof InlineIfExpression) {
            InlineIfExpression expression = (InlineIfExpression) node;
            if (expression.astIfTrue() != null) {
                Result result = search(expression.astIfTrue());
                if (result != null) {
                    return result;
                }
            }
            if (expression.astIfFalse() != null) {
                Result result = search(expression.astIfFalse());
                if (result != null) {
                    return result;
                }
            }
        } else if (node instanceof Cast) {
            Cast cast = (Cast) node;
            return search(cast.astOperand());
        } else if (node instanceof ConstructorInvocation && mOperation == Operation.ACTION) {
            // Identifies "new Intent(argument)" calls and, if found, continues
            // resolving the argument instead looking for the action definition
            ConstructorInvocation call = (ConstructorInvocation) node;
            String type = call.astTypeReference().getTypeName();
            if (type.equals("Intent") || type.equals(CLASS_INTENT)) {
                Expression action = call.astArguments().first();
                if (action != null) {
                    return search(action);
                }
            }
            return null;
        } else if ((node instanceof VariableReference || node instanceof Select)) {
            ResolvedNode resolved = mContext.resolve(node);
            if (resolved instanceof ResolvedField) {
                ResolvedField field = (ResolvedField) resolved;
                if (mOperation == Operation.ACTION) {
                    ResolvedAnnotation annotation = field.getAnnotation(PERMISSION_ANNOTATION);
                    if (annotation != null) {
                        return getPermissionRequirement(field, annotation);
                    }
                } else if (mOperation == Operation.READ || mOperation == Operation.WRITE) {
                    String fqn = mOperation == Operation.READ
                            ? PERMISSION_ANNOTATION_READ : PERMISSION_ANNOTATION_WRITE;
                    ResolvedAnnotation annotation = field.getAnnotation(fqn);
                    if (annotation != null) {
                        Object o = annotation.getValue();
                        if (o instanceof ResolvedAnnotation) {
                            annotation = (ResolvedAnnotation) o;
                            if (annotation.matches(PERMISSION_ANNOTATION)) {
                                return getPermissionRequirement(field, annotation);
                            }
                        } else {
                            // The complex annotations used for read/write cannot be
                            // expressed in the external annotations format, so they're inlined.
                            // (See Extractor.AnnotationData#write).
                            //
                            // Instead we've inlined the fields of the annotation on the
                            // outer one:
                            return getPermissionRequirement(field, annotation);
                        }
                    }
                } else {
                    assert false : mOperation;
                }
            } else if (node instanceof VariableReference) {
                Statement statement = getParentOfType(node, Statement.class, false);
                if (statement != null) {
                    ListIterator<Node> iterator =
                            statement.getParent().getChildren().listIterator();
                    while (iterator.hasNext()) {
                        if (iterator.next() == statement) {
                            if (iterator.hasPrevious()) { // should always be true
                                iterator.previous();
                            }
                            break;
                        }
                    }

                    String targetName = ((VariableReference)node).astIdentifier().astValue();
                    while (iterator.hasPrevious()) {
                        Node previous = iterator.previous();
                        if (previous instanceof VariableDeclaration) {
                            VariableDeclaration declaration = (VariableDeclaration) previous;
                            VariableDefinition definition = declaration.astDefinition();
                            for (VariableDefinitionEntry entry : definition
                                    .astVariables()) {
                                if (entry.astInitializer() != null
                                        && entry.astName().astValue().equals(targetName)) {
                                    return search(entry.astInitializer());
                                }
                            }
                        } else if (previous instanceof ExpressionStatement) {
                            ExpressionStatement expressionStatement =
                                    (ExpressionStatement) previous;
                            Expression expression = expressionStatement.astExpression();
                            if (expression instanceof BinaryExpression &&
                                    ((BinaryExpression) expression).astOperator()
                                            == BinaryOperator.ASSIGN) {
                                BinaryExpression binaryExpression = (BinaryExpression) expression;
                                if (targetName.equals(binaryExpression.astLeft().toString())) {
                                    return search(binaryExpression.astRight());
                                }
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    @NonNull
    private Result getPermissionRequirement(
            @NonNull ResolvedField field,
            @NonNull ResolvedAnnotation annotation) {
        PermissionRequirement requirement = PermissionRequirement.create(mContext, annotation);
        String name = field.getContainingClass().getSimpleName() + "." + field.getName();
        return new Result(mOperation, requirement, name);
    }
}
