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

import junit.framework.TestCase;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

import lombok.ast.CompilationUnit;
import lombok.ast.Expression;
import lombok.ast.ForwardingAstVisitor;
import lombok.ast.VariableDefinitionEntry;

public class ConstantEvaluatorTest extends TestCase {
    private static void check(Object expected, String source, final String targetVariable) {
        JavaContext context = LintUtilsTest.parse(source, new File("src/test/pkg/Test.java"));
        assertNotNull(context);
        CompilationUnit unit = (CompilationUnit) context.getCompilationUnit();
        assertNotNull(unit);

        // Find the expression
        final AtomicReference<Expression> reference = new AtomicReference<Expression>();
        unit.accept(new ForwardingAstVisitor() {
            @Override
            public boolean visitVariableDefinitionEntry(VariableDefinitionEntry node) {
                if (node.astName().astValue().equals(targetVariable)) {
                    reference.set(node.astInitializer());
                }
                return super.visitVariableDefinitionEntry(node);
            }
        });
        Expression expression = reference.get();
        Object actual = ConstantEvaluator.evaluate(context, expression);
        if (expected == null) {
            assertNull(actual);
        } else {
            assertNotNull("Couldn't compute value for " + source + ", expected " + expected,
                    actual);
            assertEquals(expected.getClass(), actual.getClass());
            assertEquals(expected.toString(), actual.toString());
        }
        assertEquals(expected, actual);
        if (expected instanceof String) {
            assertEquals(expected, ConstantEvaluator.evaluateString(context, expression,
                    false));
        }
    }

    private static void checkStatements(Object expected, String statementsSource,
            final String targetVariable) {
        String source = ""
                + "package test.pkg;\n"
                + "public class Test {\n"
                + "    public void test() {\n"
                + "        " + statementsSource + "\n"
                + "    }\n"
                + "    public static final int MY_INT_FIELD = 5;\n"
                + "    public static final boolean MY_BOOLEAN_FIELD = true;\n"
                + "    public static final String MY_STRING_FIELD = \"test\";\n"
                + "}\n";

        check(expected, source, targetVariable);
    }

    private static void checkExpression(Object expected, String expressionSource) {
        String source = ""
                + "package test.pkg;\n"
                + "public class Test {\n"
                + "    public void test() {\n"
                + "        Object expression = " + expressionSource + ";\n"
                + "    }\n"
                + "    public static final int MY_INT_FIELD = 5;\n"
                + "    public static final boolean MY_BOOLEAN_FIELD = true;\n"
                + "    public static final String MY_STRING_FIELD = \"test\";\n"
                + "}\n";

        check(expected, source, "expression");
    }

    public void testStrings() throws Exception {
        checkExpression(null, "null");
        checkExpression("hello", "\"hello\"");
        checkExpression("abcd", "\"ab\" + \"cd\"");
    }

    public void testBooleans() throws Exception {
        checkExpression(true, "true");
        checkExpression(false, "false");
        checkExpression(false, "false && true");
        checkExpression(true, "false || true");
        checkExpression(true, "!false");
    }

    public void testCasts() throws Exception {
        checkExpression(1, "(int)1");
        checkExpression(1L, "(long)1");
        checkExpression(1, "(int)1.1f");
        checkExpression((short)65537, "(short)65537");
        checkExpression((byte)1023, "(byte)1023");
        checkExpression(1.5, "(double)1.5f");
        checkExpression(-5.0, "(double)-5");
    }

    public void testArithmetic() throws Exception {
        checkExpression(1, "1");
        checkExpression(1L, "1L");
        checkExpression(4, "1 + 3");
        checkExpression(-2, "1 - 3");
        checkExpression(10, "2 * 5");
        checkExpression(2, "10 / 5");
        checkExpression(1, "11 % 5");
        checkExpression(8, "1 << 3");
        checkExpression(16, "32 >> 1");
        checkExpression(16, "32 >>> 1");
        checkExpression(5, "5 | 1");
        checkExpression(1, "5 & 1");
        checkExpression(~5, "~5");
        checkExpression(~(long)5, "~(long)5");
        checkExpression(~(short)5, "~(short)5");
        checkExpression(~(byte)5, "~(byte)5");
        checkExpression(-(long)5, "-(long)5");
        checkExpression(-(short)5, "-(short)5");
        checkExpression(-(byte)5, "-(byte)5");
        checkExpression(-(double)5, "-(double)5");
        checkExpression(-(float)5, "-(float)5");
        checkExpression(-2, "1 + -3");

        checkExpression(false, "11 == 5");
        checkExpression(true, "11 == 11");
        checkExpression(true, "11 != 5");
        checkExpression(false, "11 != 11");
        checkExpression(true, "11 > 5");
        checkExpression(false, "5 > 11");
        checkExpression(false, "11 < 5");
        checkExpression(true, "5 < 11");
        checkExpression(true, "11 >= 5");
        checkExpression(false, "5 >= 11");
        checkExpression(false, "11 <= 5");
        checkExpression(true, "5 <= 11");

        checkExpression(3.5f, "1.0f + 2.5f");
    }

    public void testFieldReferences() throws Exception {
        checkExpression(5, "MY_INT_FIELD");
        checkExpression("test", "MY_STRING_FIELD");
        checkExpression("prefix-test-postfix", "\"prefix-\" + MY_STRING_FIELD + \"-postfix\"");
        checkExpression(-4, "3 - (MY_INT_FIELD + 2)");
    }

    public void testStatements() throws Exception {
        checkStatements(9, ""
                        + "int x = +5;\n"
                        + "int y = x;\n"
                        + "int w;\n"
                        + "w = -1;\n"
                        + "int z = x + 5 + w;\n",
                "z");
        checkStatements("hello world", ""
                        + "String initial = \"hello\";\n"
                        + "String other;\n"
                        + "other = \" world\";\n"
                        + "String finalString = initial + other;\n",
                "finalString");
    }

    public void testConditionals() throws Exception {
        checkStatements(-5, ""
                        + "boolean condition = false;\n"
                        + "condition = !condition;\n"
                        + "int z = condition ? -5 : 4;\n",
                "z");
        checkStatements(-4, ""
                        + "boolean condition = true && false;\n"
                        + "int z = condition ? 5 : -4;\n",
                "z");
    }
}