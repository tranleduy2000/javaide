/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.tools.lint;

import static com.android.tools.lint.EcjParser.equalsCompound;
import static com.android.tools.lint.EcjParser.startsWithCompound;
import static com.android.tools.lint.client.api.JavaParser.ResolvedClass;
import static com.android.tools.lint.client.api.JavaParser.ResolvedField;
import static com.android.tools.lint.client.api.JavaParser.ResolvedMethod;
import static com.android.tools.lint.client.api.JavaParser.ResolvedNode;
import static com.android.tools.lint.client.api.JavaParser.ResolvedVariable;

import com.android.annotations.NonNull;
import com.android.tools.lint.checks.AbstractCheckTest;
import com.android.tools.lint.checks.SdCardDetector;
import com.android.tools.lint.client.api.JavaParser;
import com.android.tools.lint.client.api.JavaParser.ResolvedAnnotation;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.LintUtilsTest;
import com.android.tools.lint.detector.api.Project;
import com.google.common.collect.Lists;

import org.intellij.lang.annotations.Language;
import org.junit.Assert;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.ast.AnnotationElement;
import lombok.ast.BinaryExpression;
import lombok.ast.Block;
import lombok.ast.ClassDeclaration;
import lombok.ast.DescribedNode;
import lombok.ast.ExpressionStatement;
import lombok.ast.ForwardingAstVisitor;
import lombok.ast.Identifier;
import lombok.ast.KeywordModifier;
import lombok.ast.MethodInvocation;
import lombok.ast.Modifiers;
import lombok.ast.Node;
import lombok.ast.NormalTypeBody;
import lombok.ast.Select;
import lombok.ast.TypeReferencePart;
import lombok.ast.VariableDeclaration;
import lombok.ast.VariableDefinition;
import lombok.ast.VariableReference;
import lombok.ast.printer.SourceFormatter;
import lombok.ast.printer.SourcePrinter;
import lombok.ast.printer.TextFormatter;

public class EcjParserTest extends AbstractCheckTest {
    public void testTryCatchHang() throws Exception {
        // Ensure that we're really using this parser
        JavaParser javaParser = createClient().getJavaParser(null);
        assertNotNull(javaParser);
        assertTrue(javaParser.getClass().getName(), javaParser instanceof EcjParser);

        // See https://code.google.com/p/projectlombok/issues/detail?id=573#c6
        // With lombok.ast 0.2.1 and the parboiled-based Java parser this test will hang forever.
        assertEquals(
                "No warnings.",

                lintProject("src/test/pkg/TryCatchHang.java.txt=>src/test/pkg/TryCatchHang.java"));
    }

    public void testKitKatLanguageFeatures() throws Exception {
        String testClass = "" +
                "package test.pkg;\n" +
                "\n" +
                "import java.io.BufferedReader;\n" +
                "import java.io.FileReader;\n" +
                "import java.io.IOException;\n" +
                "import java.lang.reflect.InvocationTargetException;\n" +
                "import java.util.List;\n" +
                "import java.util.Map;\n" +
                "import java.util.TreeMap;\n" +
                "\n" +
                "public class Java7LanguageFeatureTest {\n" +
                "    public void testDiamondOperator() {\n" +
                "        Map<String, List<Integer>> map = new TreeMap<>();\n" +
                "    }\n" +
                "\n" +
                "    public int testStringSwitches(String value) {\n" +
                "        final String first = \"first\";\n" +
                "        final String second = \"second\";\n" +
                "\n" +
                "        switch (value) {\n" +
                "            case first:\n" +
                "                return 41;\n" +
                "            case second:\n" +
                "                return 42;\n" +
                "            default:\n" +
                "                return 0;\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    public String testTryWithResources(String path) throws IOException {\n" +
                "        try (BufferedReader br = new BufferedReader(new FileReader(path))) {\n" +
                "            return br.readLine();\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    public void testNumericLiterals() {\n" +
                "        int thousand = 1_000;\n" +
                "        int million = 1_000_000;\n" +
                "        int binary = 0B01010101;\n" +
                "    }\n" +
                "\n" +
                "    public void testMultiCatch() {\n" +
                "\n" +
                "        try {\n" +
                "            Class.forName(\"java.lang.Integer\").getMethod(\"toString\").invoke(null);\n" +
                "        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {\n" +
                "            e.printStackTrace();\n" +
                "        } catch (ClassNotFoundException e) {\n" +
                "            // TODO: Logging here\n" +
                "        }\n" +
                "    }\n" +
                "}\n";

        Node unit = LintUtilsTest.getCompilationUnit(testClass);
        assertNotNull(unit);

        // Now print the AST back and make sure that it contains at least the essence of the AST
        TextFormatter formatter = new TextFormatter();
        unit.accept(new SourcePrinter(formatter));
        String actual = formatter.finish();
        assertEquals(""
                + "package test.pkg;\n"
                + "\n"
                + "import java.io.BufferedReader;\n"
                + "import java.io.FileReader;\n"
                + "import java.io.IOException;\n"
                + "import java.lang.reflect.InvocationTargetException;\n"
                + "import java.util.List;\n"
                + "import java.util.Map;\n"
                + "import java.util.TreeMap;\n"
                + "\n"
                + "public class Java7LanguageFeatureTest {\n"
                + "    public void testDiamondOperator() {\n"
                + "        Map<String, List<Integer>> map = new TreeMap();\n" // missing types on rhs
                + "    }\n"
                + "    \n"
                + "    public int testStringSwitches(String value) {\n"
                + "        final String first = \"first\";\n"
                + "        final String second = \"second\";\n"
                + "        switch (value) {\n"
                + "        case first:\n"
                + "            return 41;\n"
                + "        case second:\n"
                + "            return 42;\n"
                + "        default:\n"
                + "            return 0;\n"
                + "        }\n"
                + "    }\n"
                + "    \n"
                + "    public String testTryWithResources(String path) throws IOException {\n"
                + "        try {\n" // Note how the initialization clause is gone here
                + "            return br.readLine();\n"
                + "        }\n"
                + "    }\n"
                + "    \n"
                + "    public void testNumericLiterals() {\n"
                + "        int thousand = 1_000;\n"
                + "        int million = 1_000_000;\n"
                + "        int binary = 0B01010101;\n"
                + "    }\n"
                + "    \n"
                + "    public void testMultiCatch() {\n"
                + "        try {\n"
                + "            Class.forName(\"java.lang.Integer\").getMethod(\"toString\").invoke(null);\n"
                + "        } catch (IllegalAccessException e) {\n" // Note: missing other union types
                + "            e.printStackTrace();\n"
                + "        } catch (ClassNotFoundException e) {\n"
                + "        }\n"
                + "    }\n"
                + "}",
                actual);
    }

    @SuppressWarnings("ClassNameDiffersFromFileName")
    public void testGetFields() throws Exception {
        @Language("JAVA")
        String source = ""
                + "public class FieldTest {\n"
                + "    public int field1 = 1;\n"
                + "    public int field2 = 3;\n"
                + "    public int field3 = 5;\n"
                + "    \n"
                + "    public static class Inner extends FieldTest {\n"
                + "        public int field2 = 5;\n"
                + "    }\n"
                + "}\n";

        final JavaContext context = LintUtilsTest.parse(source,
                new File("src/test/pkg/FieldTest.java"));
        assertNotNull(context);

        Node compilationUnit = context.getCompilationUnit();
        assertNotNull(compilationUnit);
        final AtomicBoolean found = new AtomicBoolean();
        compilationUnit.accept(new ForwardingAstVisitor() {
            @Override
            public boolean visitClassDeclaration(ClassDeclaration node) {
                if (node.astName().astValue().equals("Inner")) {
                    found.set(true);
                    ResolvedNode resolved = context.resolve(node);
                    assertNotNull(resolved);
                    ResolvedClass cls = (ResolvedClass) resolved;
                    List<ResolvedField> declaredFields = Lists.newArrayList(cls.getFields(false));
                    assertEquals(1, declaredFields.size());
                    assertEquals("field2", declaredFields.get(0).getName());

                    declaredFields = Lists.newArrayList(cls.getFields(true));
                    assertEquals(3, declaredFields.size());
                    assertEquals("field2", declaredFields.get(0).getName());
                    assertEquals("FieldTest.Inner", declaredFields.get(0).getContainingClassName());
                    assertEquals("field1", declaredFields.get(1).getName());
                    assertEquals("FieldTest", declaredFields.get(1).getContainingClassName());
                    assertEquals("field3", declaredFields.get(2).getName());
                    assertEquals("FieldTest", declaredFields.get(2).getContainingClassName());
                }

                return super.visitClassDeclaration(node);
            }
        });
        assertTrue(found.get());
    }

    public void testResolution() throws Exception {
        String source =
                "package test.pkg;\n" +
                "\n" +
                "import java.io.File;\n" +
                "\n" +
                "public class TypeResolutionTest {\n" +
                "    public static class Inner extends File {\n" +
                "        public float myField = 5f;\n" +
                "        public int[] myInts;\n" +
                "\n" +
                "        public Inner(File dir, String name) {\n" +
                "            super(dir, name);\n" +
                "        }\n" +
                "\n" +
                "        public void call(int arg1, double arg2) {\n" +
                "            boolean x = super.canRead();\n" +
                "            System.out.println(x);\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    @SuppressWarnings(\"all\")\n" +
                "    public static class Other {\n" +
                "         private void client(int z) {\n" +
                "             int x = z;\n" +
                "             int y = x + 5;\n" +
                "             Inner inner = new Inner(null, null);\n" +
                "             inner.myField = 6;\n" +
                "             System.out.println(inner.myInts);\n" +
                "         }\n" +
                "    }\n" +
                "}\n";

        Node unit = LintUtilsTest.getCompilationUnit(source,
                new File("src/test/pkg/TypeResolutionTest.java"));

        // Visit all nodes and assert nativeNode != null unless I expect it!
        unit.accept(new ForwardingAstVisitor() {
            @SuppressWarnings("Contract")
            @Override
            public boolean visitNode(Node node) {
                if (node.getNativeNode() == null && requiresNativeNode(node)) {
                    fail("Expected native node on node of type " +
                                    node.getClass().getSimpleName());
                }
                return super.visitNode(node);
            }

            private boolean requiresNativeNode(Node node) {
                if (node instanceof TypeReferencePart &&
                        node.getParent().getNativeNode() != null) {
                    return false;
                }

                if (node instanceof Identifier
                        || node instanceof NormalTypeBody
                        || node instanceof Block
                        || node instanceof VariableDeclaration
                        || node instanceof VariableDefinition
                        || node instanceof AnnotationElement
                        || node instanceof BinaryExpression
                        || node instanceof Modifiers
                        || node instanceof KeywordModifier) {
                    return false;
                }

                if (node instanceof VariableReference) {
                    VariableReference reference = (VariableReference)node;
                    if (reference.getParent() instanceof Select) {
                        return false;
                    }
                } else if (node instanceof MethodInvocation) {
                    Node parent = node.getParent();
                    if (parent instanceof ExpressionStatement &&
                            parent.getNativeNode() != null) {
                        return false;
                    }
                }

                return true;
            }
        });

        JavaParser parser = new EcjParser(new LintCliClient(), null);
        AstPrettyPrinter astPrettyPrinter = new AstPrettyPrinter(parser);
        unit.accept(new SourcePrinter(astPrettyPrinter));
        String actual = astPrettyPrinter.finish();
        assertEquals(
                "[CompilationUnit]\n" +
                "  [PackageDeclaration]\n" +
                "    [Identifier test]\n" +
                "      PROPERTY: name = test\n" +
                "    [Identifier pkg]\n" +
                "      PROPERTY: name = pkg\n" +
                "  [ImportDeclaration]\n" +
                "    PROPERTY: static = false\n" +
                "    PROPERTY: star = false\n" +
                "    [Identifier java]\n" +
                "      PROPERTY: name = java\n" +
                "    [Identifier io]\n" +
                "      PROPERTY: name = io\n" +
                "    [Identifier File]\n" +
                "      PROPERTY: name = File\n" +
                "  [ClassDeclaration TypeResolutionTest], type: test.pkg.TypeResolutionTest, resolved class: test.pkg.TypeResolutionTest \n" +
                "    [Modifiers], type: test.pkg.TypeResolutionTest, resolved class: test.pkg.TypeResolutionTest \n" +
                "      [KeywordModifier public]\n" +
                "        PROPERTY: modifier = public\n" +
                "    typeName: [Identifier TypeResolutionTest], type: test.pkg.TypeResolutionTest, resolved class: test.pkg.TypeResolutionTest \n" +
                "      PROPERTY: name = TypeResolutionTest\n" +
                "    [NormalTypeBody], type: test.pkg.TypeResolutionTest, resolved class: test.pkg.TypeResolutionTest \n" +
                "        [ClassDeclaration Inner], type: test.pkg.TypeResolutionTest.Inner, resolved class: test.pkg.TypeResolutionTest.Inner \n" +
                "          [Modifiers], type: test.pkg.TypeResolutionTest.Inner, resolved class: test.pkg.TypeResolutionTest.Inner \n" +
                "            [KeywordModifier public]\n" +
                "              PROPERTY: modifier = public\n" +
                "            [KeywordModifier static]\n" +
                "              PROPERTY: modifier = static\n" +
                "          typeName: [Identifier Inner], type: test.pkg.TypeResolutionTest.Inner, resolved class: test.pkg.TypeResolutionTest.Inner \n" +
                "            PROPERTY: name = Inner\n" +
                "          extends: [TypeReference File], type: java.io.File, resolved class: java.io.File \n" +
                "            PROPERTY: WildcardKind = NONE\n" +
                "            PROPERTY: arrayDimensions = 0\n" +
                "            [TypeReferencePart], type: java.io.File, resolved class: java.io.File \n" +
                "              [Identifier File]\n" +
                "                PROPERTY: name = File\n" +
                "          [NormalTypeBody], type: test.pkg.TypeResolutionTest.Inner, resolved class: test.pkg.TypeResolutionTest.Inner \n" +
                "              [VariableDeclaration], type: float, resolved class: float \n" +
                "                [VariableDefinition]\n" +
                "                  PROPERTY: varargs = false\n" +
                "                  [Modifiers]\n" +
                "                    [KeywordModifier public]\n" +
                "                      PROPERTY: modifier = public\n" +
                "                  type: [TypeReference float], type: float, resolved class: float \n" +
                "                    PROPERTY: WildcardKind = NONE\n" +
                "                    PROPERTY: arrayDimensions = 0\n" +
                "                    [TypeReferencePart], type: float, resolved class: float \n" +
                "                      [Identifier float]\n" +
                "                        PROPERTY: name = float\n" +
                "                  [VariableDefinitionEntry], resolved field: myField test.pkg.TypeResolutionTest.Inner\n" +
                "                    PROPERTY: arrayDimensions = 0\n" +
                "                    varName: [Identifier myField], resolved field: myField test.pkg.TypeResolutionTest.Inner\n" +
                "                      PROPERTY: name = myField\n" +
                "                    [FloatingPointLiteral 5.0], type: float\n" +
                "                      PROPERTY: value = 5f\n" +
                "              [VariableDeclaration], type: int[], resolved class: int[] \n" +
                "                [VariableDefinition]\n" +
                "                  PROPERTY: varargs = false\n" +
                "                  [Modifiers]\n" +
                "                    [KeywordModifier public]\n" +
                "                      PROPERTY: modifier = public\n" +
                "                  type: [TypeReference int[]], type: int[], resolved class: int[] \n" +
                "                    PROPERTY: WildcardKind = NONE\n" +
                "                    PROPERTY: arrayDimensions = 1\n" +
                "                    [TypeReferencePart], type: int[], resolved class: int[] \n" +
                "                      [Identifier int]\n" +
                "                        PROPERTY: name = int\n" +
                "                  [VariableDefinitionEntry], resolved field: myInts test.pkg.TypeResolutionTest.Inner\n" +
                "                    PROPERTY: arrayDimensions = 0\n" +
                "                    varName: [Identifier myInts], resolved field: myInts test.pkg.TypeResolutionTest.Inner\n" +
                "                      PROPERTY: name = myInts\n" +
                "              [ConstructorDeclaration], type: void, resolved method: test.pkg.TypeResolutionTest.Inner test.pkg.TypeResolutionTest.Inner\n" +
                "                [Modifiers], type: void, resolved method: test.pkg.TypeResolutionTest.Inner test.pkg.TypeResolutionTest.Inner\n" +
                "                  [KeywordModifier public]\n" +
                "                    PROPERTY: modifier = public\n" +
                "                typeName: [Identifier Inner], type: void, resolved method: test.pkg.TypeResolutionTest.Inner test.pkg.TypeResolutionTest.Inner\n" +
                "                  PROPERTY: name = Inner\n" +
                "                parameter: [VariableDefinition], type: void, resolved method: test.pkg.TypeResolutionTest.Inner test.pkg.TypeResolutionTest.Inner\n" +
                "                  PROPERTY: varargs = false\n" +
                "                  [Modifiers]\n" +
                "                  type: [TypeReference File], type: java.io.File, resolved class: java.io.File \n" +
                "                    PROPERTY: WildcardKind = NONE\n" +
                "                    PROPERTY: arrayDimensions = 0\n" +
                "                    [TypeReferencePart], type: java.io.File, resolved class: java.io.File \n" +
                "                      [Identifier File]\n" +
                "                        PROPERTY: name = File\n" +
                "                  [VariableDefinitionEntry], resolved variable: dir java.io.File\n" +
                "                    PROPERTY: arrayDimensions = 0\n" +
                "                    varName: [Identifier dir], resolved variable: dir java.io.File\n" +
                "                      PROPERTY: name = dir\n" +
                "                parameter: [VariableDefinition], type: void, resolved method: test.pkg.TypeResolutionTest.Inner test.pkg.TypeResolutionTest.Inner\n" +
                "                  PROPERTY: varargs = false\n" +
                "                  [Modifiers]\n" +
                "                  type: [TypeReference String], type: java.lang.String, resolved class: java.lang.String \n" +
                "                    PROPERTY: WildcardKind = NONE\n" +
                "                    PROPERTY: arrayDimensions = 0\n" +
                "                    [TypeReferencePart], type: java.lang.String, resolved class: java.lang.String \n" +
                "                      [Identifier String]\n" +
                "                        PROPERTY: name = String\n" +
                "                  [VariableDefinitionEntry], resolved variable: name java.lang.String\n" +
                "                    PROPERTY: arrayDimensions = 0\n" +
                "                    varName: [Identifier name], resolved variable: name java.lang.String\n" +
                "                      PROPERTY: name = name\n" +
                "                [Block], type: void, resolved method: test.pkg.TypeResolutionTest.Inner test.pkg.TypeResolutionTest.Inner\n" +
                "                    [SuperConstructorInvocation], resolved method: java.io.File java.io.File\n" +
                "                      [VariableReference], type: java.io.File, resolved variable: dir java.io.File\n" +
                "                        [Identifier dir], type: java.io.File, resolved variable: dir java.io.File\n" +
                "                          PROPERTY: name = dir\n" +
                "                      [VariableReference], type: java.lang.String, resolved variable: name java.lang.String\n" +
                "                        [Identifier name], type: java.lang.String, resolved variable: name java.lang.String\n" +
                "                          PROPERTY: name = name\n" +
                "              [MethodDeclaration call], type: void, resolved method: call test.pkg.TypeResolutionTest.Inner\n" +
                "                [Modifiers], type: void, resolved method: call test.pkg.TypeResolutionTest.Inner\n" +
                "                  [KeywordModifier public]\n" +
                "                    PROPERTY: modifier = public\n" +
                "                returnType: [TypeReference void], type: void, resolved class: void \n" +
                "                  PROPERTY: WildcardKind = NONE\n" +
                "                  PROPERTY: arrayDimensions = 0\n" +
                "                  [TypeReferencePart], type: void, resolved class: void \n" +
                "                    [Identifier void]\n" +
                "                      PROPERTY: name = void\n" +
                "                methodName: [Identifier call], type: void, resolved method: call test.pkg.TypeResolutionTest.Inner\n" +
                "                  PROPERTY: name = call\n" +
                "                parameter: [VariableDefinition], type: void, resolved method: call test.pkg.TypeResolutionTest.Inner\n" +
                "                  PROPERTY: varargs = false\n" +
                "                  [Modifiers]\n" +
                "                  type: [TypeReference int], type: int, resolved class: int \n" +
                "                    PROPERTY: WildcardKind = NONE\n" +
                "                    PROPERTY: arrayDimensions = 0\n" +
                "                    [TypeReferencePart], type: int, resolved class: int \n" +
                "                      [Identifier int]\n" +
                "                        PROPERTY: name = int\n" +
                "                  [VariableDefinitionEntry], resolved variable: arg1 int\n" +
                "                    PROPERTY: arrayDimensions = 0\n" +
                "                    varName: [Identifier arg1], resolved variable: arg1 int\n" +
                "                      PROPERTY: name = arg1\n" +
                "                parameter: [VariableDefinition], type: void, resolved method: call test.pkg.TypeResolutionTest.Inner\n" +
                "                  PROPERTY: varargs = false\n" +
                "                  [Modifiers]\n" +
                "                  type: [TypeReference double], type: double, resolved class: double \n" +
                "                    PROPERTY: WildcardKind = NONE\n" +
                "                    PROPERTY: arrayDimensions = 0\n" +
                "                    [TypeReferencePart], type: double, resolved class: double \n" +
                "                      [Identifier double]\n" +
                "                        PROPERTY: name = double\n" +
                "                  [VariableDefinitionEntry], resolved variable: arg2 double\n" +
                "                    PROPERTY: arrayDimensions = 0\n" +
                "                    varName: [Identifier arg2], resolved variable: arg2 double\n" +
                "                      PROPERTY: name = arg2\n" +
                "                [Block], type: void, resolved method: call test.pkg.TypeResolutionTest.Inner\n" +
                "                    [VariableDeclaration], type: boolean, resolved class: boolean \n" +
                "                      [VariableDefinition]\n" +
                "                        PROPERTY: varargs = false\n" +
                "                        [Modifiers]\n" +
                "                        type: [TypeReference boolean], type: boolean, resolved class: boolean \n" +
                "                          PROPERTY: WildcardKind = NONE\n" +
                "                          PROPERTY: arrayDimensions = 0\n" +
                "                          [TypeReferencePart], type: boolean, resolved class: boolean \n" +
                "                            [Identifier boolean]\n" +
                "                              PROPERTY: name = boolean\n" +
                "                        [VariableDefinitionEntry], resolved variable: x boolean\n" +
                "                          PROPERTY: arrayDimensions = 0\n" +
                "                          varName: [Identifier x], resolved variable: x boolean\n" +
                "                            PROPERTY: name = x\n" +
                "                          [MethodInvocation canRead], type: boolean, resolved method: canRead java.io.File\n" +
                "                            operand: [Super], type: java.io.File\n" +
                "                            methodName: [Identifier canRead], type: boolean, resolved method: canRead java.io.File\n" +
                "                              PROPERTY: name = canRead\n" +
                "                    [ExpressionStatement], type: void, resolved method: println java.io.PrintStream\n" +
                "                      [MethodInvocation println], type: void, resolved method: println java.io.PrintStream\n" +
                "                        operand: [Select], type: java.io.PrintStream, resolved field: out java.lang.System\n" +
                "                          operand: [VariableReference], type: java.io.PrintStream, resolved field: out java.lang.System\n" +
                "                            [Identifier System]\n" +
                "                              PROPERTY: name = System\n" +
                "                          selected: [Identifier out], type: java.io.PrintStream, resolved field: out java.lang.System\n" +
                "                            PROPERTY: name = out\n" +
                "                        methodName: [Identifier println]\n" +
                "                          PROPERTY: name = println\n" +
                "                        [VariableReference], type: boolean, resolved variable: x boolean\n" +
                "                          [Identifier x], type: boolean, resolved variable: x boolean\n" +
                "                            PROPERTY: name = x\n" +
                "        [ClassDeclaration Other], type: test.pkg.TypeResolutionTest.Other, resolved class: test.pkg.TypeResolutionTest.Other \n" +
                "          [Modifiers], type: test.pkg.TypeResolutionTest.Other, resolved class: test.pkg.TypeResolutionTest.Other \n" +
                "            [Annotation SuppressWarnings], type: java.lang.SuppressWarnings, resolved annotation: java.lang.SuppressWarnings \n" +
                "              [TypeReference SuppressWarnings], type: java.lang.SuppressWarnings, resolved class: java.lang.SuppressWarnings \n" +
                "                PROPERTY: WildcardKind = NONE\n" +
                "                PROPERTY: arrayDimensions = 0\n" +
                "                [TypeReferencePart], type: java.lang.SuppressWarnings, resolved class: java.lang.SuppressWarnings \n" +
                "                  [Identifier SuppressWarnings]\n" +
                "                    PROPERTY: name = SuppressWarnings\n" +
                "              [AnnotationElement null], type: java.lang.SuppressWarnings, resolved annotation: java.lang.SuppressWarnings \n" +
                "                [StringLiteral all], type: java.lang.String\n" +
                "                  PROPERTY: value = \"all\"\n" +
                "            [KeywordModifier public]\n" +
                "              PROPERTY: modifier = public\n" +
                "            [KeywordModifier static]\n" +
                "              PROPERTY: modifier = static\n" +
                "          typeName: [Identifier Other], type: test.pkg.TypeResolutionTest.Other, resolved class: test.pkg.TypeResolutionTest.Other \n" +
                "            PROPERTY: name = Other\n" +
                "          [NormalTypeBody], type: test.pkg.TypeResolutionTest.Other, resolved class: test.pkg.TypeResolutionTest.Other \n" +
                "              [MethodDeclaration client], type: void, resolved method: client test.pkg.TypeResolutionTest.Other\n" +
                "                [Modifiers], type: void, resolved method: client test.pkg.TypeResolutionTest.Other\n" +
                "                  [KeywordModifier private]\n" +
                "                    PROPERTY: modifier = private\n" +
                "                returnType: [TypeReference void], type: void, resolved class: void \n" +
                "                  PROPERTY: WildcardKind = NONE\n" +
                "                  PROPERTY: arrayDimensions = 0\n" +
                "                  [TypeReferencePart], type: void, resolved class: void \n" +
                "                    [Identifier void]\n" +
                "                      PROPERTY: name = void\n" +
                "                methodName: [Identifier client], type: void, resolved method: client test.pkg.TypeResolutionTest.Other\n" +
                "                  PROPERTY: name = client\n" +
                "                parameter: [VariableDefinition], type: void, resolved method: client test.pkg.TypeResolutionTest.Other\n" +
                "                  PROPERTY: varargs = false\n" +
                "                  [Modifiers]\n" +
                "                  type: [TypeReference int], type: int, resolved class: int \n" +
                "                    PROPERTY: WildcardKind = NONE\n" +
                "                    PROPERTY: arrayDimensions = 0\n" +
                "                    [TypeReferencePart], type: int, resolved class: int \n" +
                "                      [Identifier int]\n" +
                "                        PROPERTY: name = int\n" +
                "                  [VariableDefinitionEntry], resolved variable: z int\n" +
                "                    PROPERTY: arrayDimensions = 0\n" +
                "                    varName: [Identifier z], resolved variable: z int\n" +
                "                      PROPERTY: name = z\n" +
                "                [Block], type: void, resolved method: client test.pkg.TypeResolutionTest.Other\n" +
                "                    [VariableDeclaration], type: int, resolved class: int \n" +
                "                      [VariableDefinition]\n" +
                "                        PROPERTY: varargs = false\n" +
                "                        [Modifiers]\n" +
                "                        type: [TypeReference int], type: int, resolved class: int \n" +
                "                          PROPERTY: WildcardKind = NONE\n" +
                "                          PROPERTY: arrayDimensions = 0\n" +
                "                          [TypeReferencePart], type: int, resolved class: int \n" +
                "                            [Identifier int]\n" +
                "                              PROPERTY: name = int\n" +
                "                        [VariableDefinitionEntry], resolved variable: x int\n" +
                "                          PROPERTY: arrayDimensions = 0\n" +
                "                          varName: [Identifier x], resolved variable: x int\n" +
                "                            PROPERTY: name = x\n" +
                "                          [VariableReference], type: int, resolved variable: z int\n" +
                "                            [Identifier z], type: int, resolved variable: z int\n" +
                "                              PROPERTY: name = z\n" +
                "                    [VariableDeclaration], type: int, resolved class: int \n" +
                "                      [VariableDefinition]\n" +
                "                        PROPERTY: varargs = false\n" +
                "                        [Modifiers]\n" +
                "                        type: [TypeReference int], type: int, resolved class: int \n" +
                "                          PROPERTY: WildcardKind = NONE\n" +
                "                          PROPERTY: arrayDimensions = 0\n" +
                "                          [TypeReferencePart], type: int, resolved class: int \n" +
                "                            [Identifier int]\n" +
                "                              PROPERTY: name = int\n" +
                "                        [VariableDefinitionEntry], resolved variable: y int\n" +
                "                          PROPERTY: arrayDimensions = 0\n" +
                "                          varName: [Identifier y], resolved variable: y int\n" +
                "                            PROPERTY: name = y\n" +
                "                          [BinaryExpression +], type: int\n" +
                "                            PROPERTY: operator = +\n" +
                "                            left: [VariableReference], type: int, resolved variable: x int\n" +
                "                              [Identifier x], type: int, resolved variable: x int\n" +
                "                                PROPERTY: name = x\n" +
                "                            right: [IntegralLiteral 5], type: int\n" +
                "                              PROPERTY: value = 5\n" +
                "                    [VariableDeclaration], type: test.pkg.TypeResolutionTest.Inner, resolved class: test.pkg.TypeResolutionTest.Inner \n" +
                "                      [VariableDefinition]\n" +
                "                        PROPERTY: varargs = false\n" +
                "                        [Modifiers]\n" +
                "                        type: [TypeReference Inner], type: test.pkg.TypeResolutionTest.Inner, resolved class: test.pkg.TypeResolutionTest.Inner \n" +
                "                          PROPERTY: WildcardKind = NONE\n" +
                "                          PROPERTY: arrayDimensions = 0\n" +
                "                          [TypeReferencePart], type: test.pkg.TypeResolutionTest.Inner, resolved class: test.pkg.TypeResolutionTest.Inner \n" +
                "                            [Identifier Inner]\n" +
                "                              PROPERTY: name = Inner\n" +
                "                        [VariableDefinitionEntry], resolved variable: inner test.pkg.TypeResolutionTest.Inner\n" +
                "                          PROPERTY: arrayDimensions = 0\n" +
                "                          varName: [Identifier inner], resolved variable: inner test.pkg.TypeResolutionTest.Inner\n" +
                "                            PROPERTY: name = inner\n" +
                "                          [ConstructorInvocation Inner], type: test.pkg.TypeResolutionTest.Inner, resolved method: test.pkg.TypeResolutionTest.Inner test.pkg.TypeResolutionTest.Inner\n" +
                "                            type: [TypeReference Inner], type: test.pkg.TypeResolutionTest.Inner, resolved class: test.pkg.TypeResolutionTest.Inner \n" +
                "                              PROPERTY: WildcardKind = NONE\n" +
                "                              PROPERTY: arrayDimensions = 0\n" +
                "                              [TypeReferencePart], type: test.pkg.TypeResolutionTest.Inner, resolved class: test.pkg.TypeResolutionTest.Inner \n" +
                "                                [Identifier Inner]\n" +
                "                                  PROPERTY: name = Inner\n" +
                "                            [NullLiteral], type: null\n" +
                "                            [NullLiteral], type: null\n" +
                "                    [ExpressionStatement], type: float\n" +
                "                      [BinaryExpression =], type: float\n" +
                "                        PROPERTY: operator = =\n" +
                "                        left: [Select], type: float, resolved variable: inner test.pkg.TypeResolutionTest.Inner\n" +
                "                          operand: [VariableReference], type: float, resolved variable: inner test.pkg.TypeResolutionTest.Inner\n" +
                "                            [Identifier inner]\n" +
                "                              PROPERTY: name = inner\n" +
                "                          selected: [Identifier myField], type: float, resolved variable: inner test.pkg.TypeResolutionTest.Inner\n" +
                "                            PROPERTY: name = myField\n" +
                "                        right: [IntegralLiteral 6], type: int\n" +
                "                          PROPERTY: value = 6\n" +
                "                    [ExpressionStatement], type: void, resolved method: println java.io.PrintStream\n" +
                "                      [MethodInvocation println], type: void, resolved method: println java.io.PrintStream\n" +
                "                        operand: [Select], type: java.io.PrintStream, resolved field: out java.lang.System\n" +
                "                          operand: [VariableReference], type: java.io.PrintStream, resolved field: out java.lang.System\n" +
                "                            [Identifier System]\n" +
                "                              PROPERTY: name = System\n" +
                "                          selected: [Identifier out], type: java.io.PrintStream, resolved field: out java.lang.System\n" +
                "                            PROPERTY: name = out\n" +
                "                        methodName: [Identifier println]\n" +
                "                          PROPERTY: name = println\n" +
                "                        [Select], type: int[], resolved variable: inner test.pkg.TypeResolutionTest.Inner\n" +
                "                          operand: [VariableReference], type: int[], resolved variable: inner test.pkg.TypeResolutionTest.Inner\n" +
                "                            [Identifier inner]\n" +
                "                              PROPERTY: name = inner\n" +
                "                          selected: [Identifier myInts], type: int[], resolved variable: inner test.pkg.TypeResolutionTest.Inner\n" +
                "                            PROPERTY: name = myInts\n",
                actual);
    }

    public void testStartsWithCompound() throws Exception {
        assertTrue(startsWithCompound("test.pkg",
                new char[][]{
                        "test".toCharArray(),
                        "pkg".toCharArray()
                }));

        assertTrue(startsWithCompound("test.pkg",
                new char[][]{
                        "test".toCharArray(),
                        "pkg".toCharArray(),
                        "other".toCharArray(),
                }));

        assertFalse(startsWithCompound("test.pkg",
                new char[][]{
                        "test".toCharArray(),
                        "other".toCharArray()
                }));

        // Corner cases
        assertFalse(startsWithCompound("test.pk",
                new char[][]{
                        "test".toCharArray(),
                        "pkg".toCharArray(),
                }));
        assertFalse(startsWithCompound("test.pkg",
                new char[][]{
                        "test".toCharArray()
                }));
        assertTrue(startsWithCompound("test.",
                new char[][]{
                        "test".toCharArray(),
                        "pkg".toCharArray()
                }));
        assertFalse(startsWithCompound("test.pkg",
                new char[][]{
                        "test".toCharArray(),
                        "pk".toCharArray()
                }));
    }

    public void testEqualsWithCompound() throws Exception {
        assertTrue(equalsCompound("test.pkg",
                new char[][]{
                        "test".toCharArray(),
                        "pkg".toCharArray()
                }));

        assertFalse(equalsCompound("test.pkg",
                new char[][]{
                        "test".toCharArray(),
                        "pkg".toCharArray(),
                        "other".toCharArray(),
                }));

        assertFalse(equalsCompound("test.pkg",
                new char[][]{
                        "test".toCharArray(),
                        "other".toCharArray()
                }));

        // Corner cases
        assertFalse(equalsCompound("test.pk",
                new char[][]{
                        "test".toCharArray(),
                        "pkg".toCharArray(),
                }));
        assertFalse(equalsCompound("test.pkg",
                new char[][]{
                        "test".toCharArray()
                }));
        assertFalse(equalsCompound("test.",
                new char[][]{
                        "test".toCharArray(),
                        "pkg".toCharArray()
                }));
        assertFalse(equalsCompound("test.pkg",
                new char[][]{
                        "test".toCharArray(),
                        "pk".toCharArray()
                }));
    }

    @Override
    protected Detector getDetector() {
        return new SdCardDetector();
    }

    @Override
    protected TestLintClient createClient() {
        return new TestLintClient() {
            @NonNull
            @Override
            protected ClassPathInfo getClassPath(@NonNull Project project) {
                ClassPathInfo classPath = super.getClassPath(project);
                // Insert fake classpath entries (non existent directories) to
                // make sure the parser handles that gracefully. See issue 87740.
                classPath.getLibraries().add(new File("nonexistent path"));
                return classPath;
            }
        };
    }

    public static class AstPrettyPrinter implements SourceFormatter {

        private final StringBuilder mOutput = new StringBuilder(1000);

        private final JavaParser mResolver;

        private int mIndent;

        private String mName;

        public AstPrettyPrinter(JavaParser resolver) {
            mResolver = resolver;
        }

        private void add(String in, Object... args) {
            for (int i = 0; i < mIndent; i++) {
                mOutput.append("  ");
            }
            if (mName != null) {
                mOutput.append(mName).append(": ");
                mName = null;
            }
            if (args.length == 0) {
                mOutput.append(in);
            } else {
                mOutput.append(String.format(in, args));
            }
        }

        @Override
        public void buildInline(Node node) {
            buildNode(node);
        }

        @Override
        public void buildBlock(Node node) {
            buildNode(node);
        }

        private void buildNode(Node node) {
            if (node == null) {
                mIndent++;
                return;
            }
            String name = node.getClass().getSimpleName();
            String description = "";
            if (node instanceof DescribedNode) {
                description = " " + ((DescribedNode) node).getDescription();
            }

            String typeDescription = "";
            String resolutionDescription = "";
            JavaParser.TypeDescriptor t = mResolver.getType(null, node);
            if (t != null) {
                typeDescription = ", type: " + t.getName();
            }
            ResolvedNode resolved = mResolver.resolve(null, node);
            if (resolved != null) {
                String c = "unknown";
                String extra = "";
                if (resolved instanceof ResolvedClass) {
                    c = "class";
                } else if (resolved instanceof ResolvedMethod) {
                    c = "method";
                    ResolvedMethod method = (ResolvedMethod) resolved;
                    extra = method.getContainingClass().getName();
                } else if (resolved instanceof ResolvedField) {
                    c = "field";
                    ResolvedField field = (ResolvedField) resolved;
                    extra = field.getContainingClass().getName();
                } else if (resolved instanceof ResolvedVariable) {
                    c = "variable";
                    ResolvedVariable variable = (ResolvedVariable) resolved;
                    extra = variable.getType().getName();
                } else if (resolved instanceof ResolvedAnnotation) {
                    c = "annotation";
                }
                resolutionDescription = String.format(", resolved %1$s: %2$s %3$s",
                        c, resolved.getName(), extra);
            }

            add("[%1$s%2$s]%3$s%4$s\n", name, description, typeDescription, resolutionDescription);

            mIndent++;
        }

        @Override
        public void fail(String fail) {
            Assert.fail(fail);
        }

        @Override
        public void property(String name, Object value) {
            add("PROPERTY: %s = %s\n", name, value);
        }

        @Override
        public void keyword(String text) {
        }

        @Override
        public void operator(String text) {
        }

        @Override
        public void verticalSpace() {
        }

        @Override
        public void space() {
        }

        @Override
        public void append(String text) {
        }

        @Override
        public void startSuppressBlock() {
        }

        @Override
        public void endSuppressBlock() {
        }

        @Override
        public void startSuppressIndent() {
        }

        @Override
        public void endSuppressIndent() {
        }

        @Override
        public void closeInline() {
            mIndent--;
        }

        @Override
        public void closeBlock() {
            mIndent--;
        }

        @Override
        public void addError(int start, int end, String message) {
            fail(message);
        }

        @Override
        public String finish() {
            return mOutput.toString();
        }

        @Override
        public void setTimeTaken(long taken) {
        }

        @Override
        public void nameNextElement(String name) {
            mName = name;
        }
    }
}
