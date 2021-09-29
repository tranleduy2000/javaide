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
package com.android.tools.lint.detector.api;

import static com.android.tools.lint.detector.api.JavaContext.getParentOfType;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.tools.lint.client.api.JavaParser.ResolvedField;
import com.android.tools.lint.client.api.JavaParser.ResolvedNode;

import java.util.ListIterator;

import lombok.ast.BinaryExpression;
import lombok.ast.BinaryOperator;
import lombok.ast.BooleanLiteral;
import lombok.ast.Cast;
import lombok.ast.Expression;
import lombok.ast.ExpressionStatement;
import lombok.ast.FloatingPointLiteral;
import lombok.ast.InlineIfExpression;
import lombok.ast.IntegralLiteral;
import lombok.ast.Node;
import lombok.ast.NullLiteral;
import lombok.ast.Select;
import lombok.ast.Statement;
import lombok.ast.StringLiteral;
import lombok.ast.UnaryExpression;
import lombok.ast.UnaryOperator;
import lombok.ast.VariableDeclaration;
import lombok.ast.VariableDefinition;
import lombok.ast.VariableDefinitionEntry;
import lombok.ast.VariableReference;

/** Evaluates constant expressions */
public class ConstantEvaluator {
    private final JavaContext mContext;
    private boolean mAllowUnknown;

    /**
     * Creates a new constant evaluator
     *
     * @param context the context to use to resolve field references, if any
     */
    public ConstantEvaluator(@Nullable JavaContext context) {
        mContext = context;
    }

    /**
     * Whether we allow computing values where some terms are unknown. For example, the expression
     * {@code "foo" + x + "bar"} would return {@code null} without and {@code "foobar"} with.
     *
     * @return this for constructor chaining
     */
    public ConstantEvaluator allowUnknowns() {
        mAllowUnknown = true;
        return this;
    }

    /**
     * Evaluates the given node and returns the constant value it resolves to, if any
     *
     * @param node the node to compute the constant value for
     * @return the corresponding constant value - a String, an Integer, a Float, and so on
     */
    @Nullable
    public Object evaluate(@NonNull Node node) {
        if (node instanceof NullLiteral) {
            return null;
        } else if (node instanceof BooleanLiteral) {
            return ((BooleanLiteral)node).astValue();
        } else if (node instanceof StringLiteral) {
            StringLiteral string = (StringLiteral) node;
            return string.astValue();
        } else if (node instanceof IntegralLiteral) {
            IntegralLiteral literal = (IntegralLiteral) node;
            // Don't combine to ?: since that will promote astIntValue to a long
            if (literal.astMarkedAsLong()) {
                return literal.astLongValue();
            } else {
                return literal.astIntValue();
            }
        } else if (node instanceof FloatingPointLiteral) {
            FloatingPointLiteral literal = (FloatingPointLiteral) node;
            // Don't combine to ?: since that will promote astFloatValue to a double
            if (literal.astMarkedAsFloat()) {
                return literal.astFloatValue();
            } else {
                return literal.astDoubleValue();
            }
        } else if (node instanceof UnaryExpression) {
            UnaryOperator operator = ((UnaryExpression) node).astOperator();
            Object operand = evaluate(((UnaryExpression) node).astOperand());
            if (operand == null) {
                return null;
            }
            switch (operator) {
                case LOGICAL_NOT:
                    if (operand instanceof Boolean) {
                        return !(Boolean) operand;
                    }
                    break;
                case UNARY_PLUS:
                    return operand;
                case BINARY_NOT:
                    if (operand instanceof Integer) {
                        return ~(Integer) operand;
                    } else if (operand instanceof Long) {
                        return ~(Long) operand;
                    } else if (operand instanceof Short) {
                        return ~(Short) operand;
                    } else if (operand instanceof Character) {
                        return ~(Character) operand;
                    } else if (operand instanceof Byte) {
                        return ~(Byte) operand;
                    }
                    break;
                case UNARY_MINUS:
                    if (operand instanceof Integer) {
                        return -(Integer) operand;
                    } else if (operand instanceof Long) {
                        return -(Long) operand;
                    } else if (operand instanceof Double) {
                        return -(Double) operand;
                    } else if (operand instanceof Float) {
                        return -(Float) operand;
                    } else if (operand instanceof Short) {
                        return -(Short) operand;
                    } else if (operand instanceof Character) {
                        return -(Character) operand;
                    } else if (operand instanceof Byte) {
                        return -(Byte) operand;
                    }
                    break;
            }
        } else if (node instanceof InlineIfExpression) {
            InlineIfExpression expression = (InlineIfExpression) node;
            Object known = evaluate(expression.astCondition());
            if (known == Boolean.TRUE && expression.astIfTrue() != null) {
                return evaluate(expression.astIfTrue());
            } else if (known == Boolean.FALSE && expression.astIfFalse() != null) {
                return evaluate(expression.astIfFalse());
            }
        } else if (node instanceof BinaryExpression) {
            BinaryOperator operator = ((BinaryExpression) node).astOperator();
            Object operandLeft = evaluate(((BinaryExpression) node).astLeft());
            Object operandRight = evaluate(((BinaryExpression) node).astRight());
            if (operandLeft == null || operandRight == null) {
                if (mAllowUnknown) {
                    if (operandLeft == null) {
                        return operandRight;
                    } else {
                        return operandLeft;
                    }
                }
                return null;
            }
            if (operandLeft instanceof String && operandRight instanceof String) {
                if (operator == BinaryOperator.PLUS) {
                    return operandLeft.toString() + operandRight.toString();
                }
                return null;
            } else if (operandLeft instanceof Boolean && operandRight instanceof Boolean) {
                boolean left = (Boolean) operandLeft;
                boolean right = (Boolean) operandRight;
                switch (operator) {
                    case LOGICAL_OR:
                        return left || right;
                    case LOGICAL_AND:
                        return left && right;
                    case BITWISE_OR:
                        return left | right;
                    case BITWISE_XOR:
                        return left ^ right;
                    case BITWISE_AND:
                        return left & right;
                    case EQUALS:
                        return left == right;
                    case NOT_EQUALS:
                        return left != right;
                }
            } else if (operandLeft instanceof Number && operandRight instanceof Number) {
                Number left = (Number) operandLeft;
                Number right = (Number) operandRight;
                boolean isInteger =
                        !(left instanceof Float || left instanceof Double
                                || right instanceof Float || right instanceof Double);
                boolean isWide =
                        isInteger ? (left instanceof Long || right instanceof Long)
                                : (left instanceof Double || right instanceof Double);

                switch (operator) {
                    case BITWISE_OR:
                        if (isWide) {
                            return left.longValue() | right.longValue();
                        } else {
                            return left.intValue() | right.intValue();
                        }
                    case BITWISE_XOR:
                        if (isWide) {
                            return left.longValue() ^ right.longValue();
                        } else {
                            return left.intValue() ^ right.intValue();
                        }
                    case BITWISE_AND:
                        if (isWide) {
                            return left.longValue() & right.longValue();
                        } else {
                            return left.intValue() & right.intValue();
                        }
                    case EQUALS:
                        if (isInteger) {
                            return left.longValue() == right.longValue();
                        } else {
                            return left.doubleValue() == right.doubleValue();
                        }
                    case NOT_EQUALS:
                        if (isInteger) {
                            return left.longValue() != right.longValue();
                        } else {
                            return left.doubleValue() != right.doubleValue();
                        }
                    case GREATER:
                        if (isInteger) {
                            return left.longValue() > right.longValue();
                        } else {
                            return left.doubleValue() > right.doubleValue();
                        }
                    case GREATER_OR_EQUAL:
                        if (isInteger) {
                            return left.longValue() >= right.longValue();
                        } else {
                            return left.doubleValue() >= right.doubleValue();
                        }
                    case LESS:
                        if (isInteger) {
                            return left.longValue() < right.longValue();
                        } else {
                            return left.doubleValue() < right.doubleValue();
                        }
                    case LESS_OR_EQUAL:
                        if (isInteger) {
                            return left.longValue() <= right.longValue();
                        } else {
                            return left.doubleValue() <= right.doubleValue();
                        }
                    case SHIFT_LEFT:
                        if (isWide) {
                            return left.longValue() << right.intValue();
                        } else {
                            return left.intValue() << right.intValue();
                        }
                    case SHIFT_RIGHT:
                        if (isWide) {
                            return left.longValue() >> right.intValue();
                        } else {
                            return left.intValue() >> right.intValue();
                        }
                    case BITWISE_SHIFT_RIGHT:
                        if (isWide) {
                            return left.longValue() >>> right.intValue();
                        } else {
                            return left.intValue() >>> right.intValue();
                        }
                    case PLUS:
                        if (isInteger) {
                            if (isWide) {
                                return left.longValue() + right.longValue();
                            } else {
                                return left.intValue() + right.intValue();
                            }
                        } else {
                            if (isWide) {
                                return left.doubleValue() + right.doubleValue();
                            } else {
                                return left.floatValue() + right.floatValue();
                            }
                        }
                    case MINUS:
                        if (isInteger) {
                            if (isWide) {
                                return left.longValue() - right.longValue();
                            } else {
                                return left.intValue() - right.intValue();
                            }
                        } else {
                            if (isWide) {
                                return left.doubleValue() - right.doubleValue();
                            } else {
                                return left.floatValue() - right.floatValue();
                            }
                        }
                    case MULTIPLY:
                        if (isInteger) {
                            if (isWide) {
                                return left.longValue() * right.longValue();
                            } else {
                                return left.intValue() * right.intValue();
                            }
                        } else {
                            if (isWide) {
                                return left.doubleValue() * right.doubleValue();
                            } else {
                                return left.floatValue() * right.floatValue();
                            }
                        }
                    case DIVIDE:
                        if (isInteger) {
                            if (isWide) {
                                return left.longValue() / right.longValue();
                            } else {
                                return left.intValue() / right.intValue();
                            }
                        } else {
                            if (isWide) {
                                return left.doubleValue() / right.doubleValue();
                            } else {
                                return left.floatValue() / right.floatValue();
                            }
                        }
                    case REMAINDER:
                        if (isInteger) {
                            if (isWide) {
                                return left.longValue() % right.longValue();
                            } else {
                                return left.intValue() % right.intValue();
                            }
                        } else {
                            if (isWide) {
                                return left.doubleValue() % right.doubleValue();
                            } else {
                                return left.floatValue() % right.floatValue();
                            }
                        }
                    default:
                        return null;
                }
            }
        } else if (node instanceof Cast) {
            Cast cast = (Cast)node;
            Object operandValue = evaluate(cast.astOperand());
            if (operandValue instanceof Number) {
                Number number = (Number)operandValue;
                String typeName = cast.astTypeReference().getTypeName();
                if (typeName.equals("float")) {
                    return number.floatValue();
                } else if (typeName.equals("double")) {
                    return number.doubleValue();
                } else if (typeName.equals("int")) {
                    return number.intValue();
                } else if (typeName.equals("long")) {
                    return number.longValue();
                } else if (typeName.equals("short")) {
                    return number.shortValue();
                } else if (typeName.equals("byte")) {
                    return number.byteValue();
                }
            }
            return operandValue;
        } else if (mContext != null && (node instanceof VariableReference ||
                node instanceof Select)) {
            ResolvedNode resolved = mContext.resolve(node);
            if (resolved instanceof ResolvedField) {
                ResolvedField field = (ResolvedField) resolved;
                return field.getValue();
            } else if (node instanceof VariableReference) {
                Statement statement = getParentOfType(node, Statement.class, false);
                if (statement != null) {
                    ListIterator<Node> iterator = statement.getParent().getChildren().listIterator();
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
                                    return evaluate(entry.astInitializer());
                                }
                            }
                        } else if (previous instanceof ExpressionStatement) {
                            ExpressionStatement expressionStatement = (ExpressionStatement) previous;
                            Expression expression = expressionStatement.astExpression();
                            if (expression instanceof BinaryExpression &&
                                    ((BinaryExpression) expression).astOperator()
                                            == BinaryOperator.ASSIGN) {
                                BinaryExpression binaryExpression = (BinaryExpression) expression;
                                if (targetName.equals(binaryExpression.astLeft().toString())) {
                                    return evaluate(binaryExpression.astRight());
                                }
                            }
                        }
                    }
                }
            }
        }

        // TODO: Check for MethodInvocation and perform some common operations -
        // Math.* methods, String utility methods like notNullize, etc

        return null;
    }

    /**
     * Evaluates the given node and returns the constant value it resolves to, if any. Convenience
     * wrapper which creates a new {@linkplain ConstantEvaluator}, evaluates the node and returns
     * the result.
     *
     * @param context the context to use to resolve field references, if any
     * @param node    the node to compute the constant value for
     * @return the corresponding constant value - a String, an Integer, a Float, and so on
     */
    @Nullable
    public static Object evaluate(@NonNull JavaContext context, @NonNull Node node) {
        return new ConstantEvaluator(context).evaluate(node);
    }

    /**
     * Evaluates the given node and returns the constant string it resolves to, if any. Convenience
     * wrapper which creates a new {@linkplain ConstantEvaluator}, evaluates the node and returns
     * the result if the result is a string.
     *
     * @param context      the context to use to resolve field references, if any
     * @param node         the node to compute the constant value for
     * @param allowUnknown whether we should construct the string even if some parts of it are
     *                     unknown
     * @return the corresponding string, if any
     */
    @Nullable
    public static String evaluateString(@NonNull JavaContext context, @NonNull Node node,
            boolean allowUnknown) {
        ConstantEvaluator evaluator = new ConstantEvaluator(context);
        if (allowUnknown) {
            evaluator.allowUnknowns();
        }
        Object value = evaluator.evaluate(node);
        return value instanceof String ? (String) value : null;
    }
}
