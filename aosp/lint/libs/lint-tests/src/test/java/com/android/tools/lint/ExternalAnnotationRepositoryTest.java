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

package com.android.tools.lint;

import static com.android.tools.lint.ExternalAnnotationRepository.FN_ANNOTATIONS_XML;
import static com.google.common.base.Charsets.UTF_8;
import static java.io.File.separatorChar;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.testutils.SdkTestCase;
import com.android.tools.lint.client.api.JavaParser.DefaultTypeDescriptor;
import com.android.tools.lint.client.api.JavaParser.ResolvedAnnotation;
import com.android.tools.lint.client.api.JavaParser.ResolvedClass;
import com.android.tools.lint.client.api.JavaParser.ResolvedField;
import com.android.tools.lint.client.api.JavaParser.ResolvedMethod;
import com.android.tools.lint.client.api.JavaParser.ResolvedNode;
import com.android.tools.lint.client.api.JavaParser.ResolvedPackage;
import com.android.tools.lint.client.api.JavaParser.TypeDescriptor;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.LintUtilsTest;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.ast.ClassDeclaration;
import lombok.ast.ForwardingAstVisitor;
import lombok.ast.MethodDeclaration;
import lombok.ast.MethodInvocation;
import lombok.ast.Node;

public class ExternalAnnotationRepositoryTest extends SdkTestCase {

    @Nullable
    private ExternalAnnotationRepository getSdkAnnotations() {
        File annotations = findSrcRelativeDir("tools/adt/idea/android/annotations");
        if (annotations != null) {
            List<File> files = Collections.singletonList(annotations);
            ExternalAnnotationRepository manager = ExternalAnnotationRepository.create(null, files);
            assertNotNull(manager);
            return manager;
        } else {
            // Can't find it when running from Gradle; ignore for now
            //fail("Could not find annotations database");
        }

        return null;
    }

    @Nullable
    private ExternalAnnotationRepository getExternalAnnotations(@NonNull String pkg,
            @NonNull String contents) throws IOException {
        File dir = Files.createTempDir();
        try {
            File pkgDir = new File(dir, pkg.replace('.', separatorChar));
            boolean mkdirs = pkgDir.mkdirs();
            assertTrue(mkdirs);
            Files.write(contents, new File(pkgDir, FN_ANNOTATIONS_XML), UTF_8);

            List<File> files = Collections.singletonList(dir);
            ExternalAnnotationRepository manager = ExternalAnnotationRepository.create(null, files);
            assertNotNull(manager);
            return manager;

        } finally {
            deleteFile(dir);
        }
    }

    public void testFields() throws Exception {
        ExternalAnnotationRepository manager = getExternalAnnotations("android.graphics", ""
                + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<root>\n"
                + "  <item name=\"android.graphics.Color\">\n"
                + "    <annotation name=\"android.support.annotation.Annotation1\" />\n"
                + "  </item>\n"
                + "  <item name=\"android.graphics.Color BLUE\">\n"
                + "    <annotation name=\"android.support.annotation.Annotation3\" />\n"
                + "  </item>\n"
                + "  <item name=\"android.graphics.Color TRANSPARENT\">\n"
                + "    <annotation name=\"android.support.annotation.Annotation5\" />\n"
                + "  </item>\n"
                + "</root>\n");
        assertNotNull(manager);
        ResolvedClass cls = createClass("android.graphics.Color");
        assertNotNull(manager.getAnnotation(cls, "android.support.annotation.Annotation1"));
        ResolvedField blueField = createField("android.graphics.Color", "BLUE");
        ResolvedField transparentField = createField("android.graphics.Color", "TRANSPARENT");
        assertNotNull(manager.getAnnotation(blueField, "android.support.annotation.Annotation3"));
        assertNull(manager.getAnnotation(blueField, "android.support.annotation.Annotation4"));
        assertNull(manager.getAnnotation(blueField, "android.support.annotation.Annotation5"));

        assertNotNull(manager.getAnnotation(transparentField, "android.support.annotation.Annotation5"));
        assertNull(manager.getAnnotation(transparentField, "android.support.annotation.Annotation3"));
        assertNull(manager.getAnnotation(transparentField, "android.support.annotation.Annotation4"));
    }

    public void testMethods1() throws Exception {
        ExternalAnnotationRepository manager = getExternalAnnotations("android.graphics", ""
                + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<root>\n"
                + "  <item name=\"android.graphics.Color int HSVToColor(float[]) 0\">\n"
                + "    <annotation name=\"android.support.annotation.Annotation5\" />\n"
                + "  </item>\n"
                + "  <item name=\"android.graphics.Color int HSVToColor(int, float[]) 1\">\n"
                + "    <annotation name=\"android.support.annotation.Annotation7\">\n"
                + "      <val name=\"value\" val=\"3\" />\n"
                + "    </annotation>\n"
                + "  </item>\n"
                + "  <item name=\"android.graphics.Color int alpha(int)\">\n"
                + "    <annotation name=\"android.support.annotation.Annotation3\" />\n"
                + "  </item>\n"
                + "  <item name=\"android.graphics.Color int argb(int, int, int, int)\">\n"
                + "    <annotation name=\"android.support.annotation.Annotation3\" />\n"
                + "  </item>\n"
                + "  <item name=\"android.graphics.RadialGradient RadialGradient(float, float, float, int[], float[], android.graphics.Shader.TileMode) 4\">\n"
                + "    <annotation name=\"android.support.annotation.Annotation5\" />\n"
                + "  </item>\n"
                + "  <item name=\"android.graphics.ArrayAdapter ArrayAdapter(android.content.Context, int, int, java.util.List&lt;T&gt;) 3\">\n"
                + "    <annotation name=\"android.support.annotation.Annotation4\" />\n"
                + "  </item>"
                + "</root>\n");
        assertNotNull(manager);
        ResolvedMethod method1 = createMethod("android.graphics.Color", "int", "HSVToColor",
                "float[]");
        assertNotNull(manager.getAnnotation(method1, 0, "android.support.annotation.Annotation5"));

        // Generic types
        ResolvedMethod method2 = createConstructor("android.graphics.ArrayAdapter", "ArrayAdapter",
                "android.content.Context, int, int, java.util.List<T>");
        assertNotNull(manager.getAnnotation(method2, 3, "android.support.annotation.Annotation4"));

        // Raw types
        method2 = createConstructor("android.graphics.ArrayAdapter", "ArrayAdapter",
                "android.content.Context, int, int, java.util.List");
        assertNotNull(manager.getAnnotation(method2, 3, "android.support.annotation.Annotation4"));
    }

    public void testMethods2() throws Exception {
        ExternalAnnotationRepository manager = getExternalAnnotations("android.graphics", ""
                + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<root>\n"
                + "  <item name=\"test.pkg.Test java.lang.Object myMethod()\">\n"
                + "    <annotation name=\"android.support.annotation.Annotation5\" />\n"
                + "    <annotation name=\"android.support.annotation.Annotation6\">\n"
                + "      <val name=\"suggest\" val=\"&quot;#other(String,String)&quot;\" />\n"
                + "    </annotation>\n"
                + "  </item>\n"
                + "  <item name=\"test.pkg.Test java.lang.Object myMethod(int) 0\">\n"
                + "    <annotation name=\"android.support.annotation.Annotation7\">\n"
                + "      <val name=\"value\" val=\"3\" />\n"
                + "    </annotation>\n"
                + "  </item>\n"
                + "  <item name=\"test.pkg.Test java.lang.Object myMethod(int[]) 0\">\n"
                + "    <annotation name=\"android.support.annotation.Annotation3\" />\n"
                + "  </item>\n"
                + "  <item name=\"test.pkg.Test java.lang.Object myMethod(int,java.lang.Object) 1\">\n"
                + "    <annotation name=\"android.support.annotation.Annotation5\" />\n"
                + "  </item>\n"
                + "  <item name=\"test.pkg.Test java.lang.Object myMethod(android.content.Context, int, int, java.util.List&lt;T&gt;) 0\">\n"
                + "    <annotation name=\"android.support.annotation.Annotation5\" />\n"
                + "  </item>\n"
                + "  <item name=\"test.pkg.Test java.lang.Object myMethod(android.content.Context, int, int, java.util.List&lt;T&gt;) 1\">\n"
                + "    <annotation name=\"android.support.annotation.Annotation3\" />\n"
                + "  </item>\n"
                + "  <item name=\"test.pkg.Test java.lang.Object myMethod(java.util.Map&lt;java.lang.String,java.util.Map&lt;java.lang.String,java.lang.String&gt;&gt;,int) 0\">\n"
                + "    <annotation name=\"android.support.annotation.Annotation4\" />\n"
                + "  </item>\n"
                + "</root>\n");
        assertNotNull(manager);
        ResolvedMethod method;
        method = createMethod("test.pkg.Test", "java.lang.Object", "myMethod", "");
        assertNotNull(manager.getAnnotation(method, "android.support.annotation.Annotation5"));
        assertNull(manager.getAnnotation(method, "android.support.annotation.Annotation4"));
        assertNotNull(manager.getAnnotation(method, "android.support.annotation.Annotation6"));

        method = createMethod("test.pkg.Test", "java.lang.Object", "myMethod", "int");
        assertNull(manager.getAnnotation(method, "android.support.annotation.Annotation4"));
        assertNotNull(manager.getAnnotation(method, 0, "android.support.annotation.Annotation7"));

        method = createMethod("test.pkg.Test", "java.lang.Object", "myMethod", "int[]");
        assertNull(manager.getAnnotation(method, "android.support.annotation.Annotation4"));
        assertNull(manager.getAnnotation(method, "android.support.annotation.Annotation3"));
        assertNull(manager.getAnnotation(method, 0, "android.support.annotation.Annotation4"));
        assertNotNull(manager.getAnnotation(method, 0, "android.support.annotation.Annotation3"));

        method = createMethod("test.pkg.Test", "java.lang.Object", "myMethod",
                "int,java.lang.Object");
        assertNotNull(manager.getAnnotation(method, 1, "android.support.annotation.Annotation5"));

        method = createMethod("test.pkg.Test", "java.lang.Object", "myMethod",
                "android.content.Context, int, int, java.util.List<T>");
        assertNull(manager.getAnnotation(method, "android.support.annotation.Annotation4"));
        assertNull(manager.getAnnotation(method, "android.support.annotation.Annotation5"));
        assertNull(manager.getAnnotation(method, 0, "android.support.annotation.Annotation4"));
        assertNull(manager.getAnnotation(method, 0, "android.support.annotation.Annotation3"));
        assertNotNull(manager.getAnnotation(method, 0, "android.support.annotation.Annotation5"));
        assertNotNull(manager.getAnnotation(method, 1, "android.support.annotation.Annotation3"));
        assertNull(manager.getAnnotation(method, 1, "android.support.annotation.Annotation5"));

        method = createMethod("test.pkg.Test", "java.lang.Object", "myMethod",
                "android.content.Context, int, int, java.util.List");
        assertNotNull(manager.getAnnotation(method, 0, "android.support.annotation.Annotation5"));
        assertNotNull(manager.getAnnotation(method, 1, "android.support.annotation.Annotation3"));

        method = createMethod("test.pkg.Test", "java.lang.Object", "myMethod",
                Arrays.asList("java.util.Map<java.lang.String,java.util.Map<java.lang.String,java.lang.String>>",
                        "int"), false);
        assertNotNull(manager.getAnnotation(method, 0, "android.support.annotation.Annotation4"));
        method = createMethod("test.pkg.Test", "java.lang.Object", "myMethod",
                "java.util.Map,int");
        assertNotNull(manager.getAnnotation(method, 0, "android.support.annotation.Annotation4"));
    }

    // test intdef!


    public void testAnnotationAttributes() throws Exception {
        ExternalAnnotationRepository manager = getExternalAnnotations("android.graphics", ""
                + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<root>\n"
                + "  <item name=\"android.graphics.Color int HSVToColor(float[]) 0\">\n"
                + "    <annotation name=\"android.support.annotation.Annotation7\">\n"
                + "      <val name=\"value\" val=\"3\" />\n"
                + "    </annotation>\n"
                + "  </item>\n"
                + "  <item name=\"android.graphics.Color int HSVToColor(int, float[]) 1\">\n"
                + "    <annotation name=\"android.support.annotation.Annotation7\">\n"
                + "      <val name=\"value\" val=\"3\" />\n"
                + "    </annotation>\n"
                + "  </item>\n"
                + "  <item name=\"android.graphics.Canvas void drawLines(float[], android.graphics.Paint) 0\">\n"
                + "    <annotation name=\"android.support.annotation.Annotation7\">\n"
                + "      <val name=\"min\" val=\"4\" />\n"
                + "      <val name=\"multiple\" val=\"2\" />\n"
                + "    </annotation>\n"
                + "    <annotation name=\"android.support.annotation.Annotation4\" />\n"
                + "  </item>\n"
                + "  <item name=\"android.graphics.Canvas int saveLayer(android.graphics.RectF, android.graphics.Paint, int) 2\">\n"
                + "    <annotation name=\"android.support.annotation.Annotation8\">\n"
                + "      <val name=\"value\" val=\"{android.graphics.Canvas.MATRIX_SAVE_FLAG, android.graphics.Canvas.CLIP_SAVE_FLAG, android.graphics.Canvas.HAS_ALPHA_LAYER_SAVE_FLAG, android.graphics.Canvas.FULL_COLOR_LAYER_SAVE_FLAG, android.graphics.Canvas.CLIP_TO_LAYER_SAVE_FLAG, android.graphics.Canvas.ALL_SAVE_FLAG}\" />\n"
                + "      <val name=\"flag\" val=\"true\" />\n"
                + "    </annotation>\n"
                + "  </item>\n"
                + "</root>\n");
        assertNotNull(manager);
        ResolvedMethod method;
        ResolvedAnnotation annotation;

        // Size 1
        method = createMethod("android.graphics.Color", "int", "HSVToColor", "int, float[]");
        assertNull(manager.getAnnotation(method, "android.support.annotation.Annotation7"));
        assertNull(manager.getAnnotation(method, 0, "android.support.annotation.Annotation7"));
        annotation = manager.getAnnotation(method, 1, "android.support.annotation.Annotation7");
        assertNotNull(annotation);
        assertEquals(3L, annotation.getValue());
        assertEquals(3L, annotation.getValue("value"));
        //noinspection ConstantConditions
        assertEquals(3, ((Number)annotation.getValue("value")).intValue());
        assertNotNull(annotation);

        // Size 2
        method = createMethod("android.graphics.Canvas", "void", "drawLines", "float[], android.graphics.Paint");
        assertNotNull(manager.getAnnotation(method, 0, "android.support.annotation.Annotation4"));
        annotation = manager.getAnnotation(method, 0, "android.support.annotation.Annotation7");
        assertNotNull(annotation);
        assertEquals(4L, annotation.getValue("min"));
        assertEquals(2L, annotation.getValue("multiple"));
        assertNotNull(annotation);

        // Intdef
        method = createMethod("android.graphics.Canvas", "int", "saveLayer",
                "android.graphics.RectF, android.graphics.Paint, int");
        annotation = manager.getAnnotation(method, 2, "android.support.annotation.Annotation8");
        assertNotNull(annotation);
        assertEquals(true, annotation.getValue("flag"));
        Object[] values = (Object[]) annotation.getValue("value");
        assertNotNull(values);
        assertEquals(6, values.length);
        assertTrue(values[0] instanceof ResolvedField);
        assertFalse(values[0].equals(createField("android.graphics.Canvas", "WRONG_NAME")));
        assertEquals(values[0], createField("android.graphics.Canvas", "MATRIX_SAVE_FLAG"));
        assertEquals(values[1], createField("android.graphics.Canvas", "CLIP_SAVE_FLAG"));
        assertEquals(values[2], createField("android.graphics.Canvas", "HAS_ALPHA_LAYER_SAVE_FLAG"));
        assertEquals(values[3], createField("android.graphics.Canvas", "FULL_COLOR_LAYER_SAVE_FLAG"));
        assertEquals(values[4], createField("android.graphics.Canvas", "CLIP_TO_LAYER_SAVE_FLAG"));
        assertEquals(values[5], createField("android.graphics.Canvas", "ALL_SAVE_FLAG"));

        ResolvedField field = (ResolvedField)values[0];
        assertEquals("android.graphics.Canvas.MATRIX_SAVE_FLAG", field.getSignature());
        assertEquals("android.graphics.Canvas", field.getContainingClassName());
        assertEquals("MATRIX_SAVE_FLAG", field.getName());
    }

    public void testConstructors() throws Exception {
        ExternalAnnotationRepository manager = getExternalAnnotations("android.graphics", ""
                + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<root>\n"
                + "  <item name=\"android.graphics.RadialGradient RadialGradient(float, float, float, int[], float[], android.graphics.Shader.TileMode) 4\">\n"
                + "    <annotation name=\"android.support.annotation.Annotation5\" />\n"
                + "  </item>\n"
                + "</root>\n");
        assertNotNull(manager);
        ResolvedMethod method = createConstructor("android.graphics.RadialGradient",
                "RadialGradient",
                "float, float, float, int[], float[], android.graphics.Shader.TileMode");
        assertNull(manager.getAnnotation(method, 4, "android.support.annotation.Annotation4"));
        assertNotNull(manager.getAnnotation(method, 4, "android.support.annotation.Annotation5"));
    }

    public void testVarArgs() throws Exception {
        ExternalAnnotationRepository manager = getExternalAnnotations("android.graphics", ""
                + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<root>\n"
                + "  <item name=\"android.graphics.RadialGradient RadialGradient(float, float, float, int...) 3\">\n"
                + "    <annotation name=\"android.support.annotation.Annotation5\" />\n"
                + "  </item>\n"
                + "  <item name=\"android.graphics.Bitmap android.graphics.Bitmap extractAlpha(android.graphics.Paint, int[]) 2\">\n"
                + "    <annotation name=\"android.support.annotation.Annotation5\" />\n"
                + "  </item>\n"
                + "</root>\n");
        assertNotNull(manager);
        // Match "..." in external annotation with ... in code lookup
        ResolvedMethod method = createConstructor("android.graphics.RadialGradient",
                "RadialGradient",
                "float, float, float, int...");
        assertNotNull(manager.getAnnotation(method, 3, "android.support.annotation.Annotation5"));
        // Match "..." in external annotation with [] in code lookup
        method = createConstructor("android.graphics.RadialGradient",
                "RadialGradient",
                "float, float, float, int[]");
        assertNotNull(manager.getAnnotation(method, 3, "android.support.annotation.Annotation5"));

        // Match "[]" in external annotation with [] in code lookup
        method = createMethod("android.graphics.Bitmap",
                "android.graphics.Bitmap",
                "extractAlpha",
                "android.graphics.Paint, int[]");
        assertNotNull(manager.getAnnotation(method, 2, "android.support.annotation.Annotation5"));

        // Match "[]" in external annotation with ... in code lookup
        method = createMethod("android.graphics.Bitmap",
                "android.graphics.Bitmap",
                "extractAlpha",
                "android.graphics.Paint, int...");
        assertNotNull(manager.getAnnotation(method, 2, "android.support.annotation.Annotation5"));
    }

    public void testPackage() throws Exception {
        ExternalAnnotationRepository manager = getExternalAnnotations("foo.bar.baz", ""
                + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<root>\n"
                + "  <item name=\"foo.bar.baz.package-info\">\n"
                + "    <annotation name=\"my.pkg.MyAnnotation\"/>\n"
                + "  </item>\n"
                + "</root>\n");
        assertNotNull(manager);
        ResolvedClass cls = createClass("foo.bar.baz.AdView");
        ResolvedPackage pkg = cls.getPackage();
        assertNotNull(pkg);
        assertNull(manager.getAnnotation(pkg, "foo.bar.Baz"));
        assertNotNull(manager.getAnnotation(pkg, "my.pkg.MyAnnotation"));
    }

    public void testMatchWithEcj() throws Exception {
        try {
            ExternalAnnotationRepository manager = getExternalAnnotations("test.pkg", ""
                    + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<root>\n"
                    + "  <item name=\"test.pkg.Test\">\n"
                    + "    <annotation name=\"android.support.annotation.Annotation1\" />\n"
                    + "  </item>\n"
                    + "  <item name=\"test.pkg.Test.Inner\">\n"
                    + "    <annotation name=\"android.support.annotation.Annotation2\" />\n"
                    + "  </item>\n"
                    + "  <item name=\"test.pkg.Test void foo(int, int[], int...)\">\n"
                    + "    <annotation name=\"android.support.annotation.Annotation6\" />\n"
                    + "  </item>\n"
                    + "  <item name=\"test.pkg.Test void foo(int, int[], int...) 0\">\n"
                    + "    <annotation name=\"android.support.annotation.Annotation3\" />\n"
                    + "  </item>\n"
                    + "  <item name=\"test.pkg.Test void foo(int, int[], int...) 1\">\n"
                    + "    <annotation name=\"android.support.annotation.Annotation3\" />\n"
                    + "    <annotation name=\"android.support.annotation.Annotation4\" />\n"
                    + "  </item>\n"
                    + "  <item name=\"test.pkg.Test void foo(int, int[], int...) 2\">\n"
                    + "    <annotation name=\"android.support.annotation.Annotation5\" />\n"
                    + "  </item>\n"
                    + "</root>\n");
            assertNotNull(manager);
            ExternalAnnotationRepository.set(manager);

            String source =
                    "package test.pkg;\n" +
                    "\n" +
                    "public class Test {\n" +
                    "    public void foo(int a, int[] b, int... c) {\n" +
                    "    }\n" +
                    "    public static class Inner {\n" +
                    "    }\n" +
                    "}\n";

            final JavaContext context = LintUtilsTest.parse(source,
                    new File("src/test/pkg/Test.java"));
            assertNotNull(context);
            Node unit = context.getCompilationUnit();
            assertNotNull(unit);
            unit.accept(new ForwardingAstVisitor() {
                @Override
                public boolean visitClassDeclaration(ClassDeclaration node) {
                    ResolvedNode resolved = context.resolve(node);
                    assertNotNull(resolved);
                    assertTrue(resolved.getClass().getName(), resolved instanceof ResolvedClass);
                    ResolvedClass cls = (ResolvedClass) resolved;
                    if (cls.getName().endsWith(".Inner")) {
                        assertNull(cls.getAnnotation("android.support.annotation.Annotation1"));
                        assertNotNull(cls.getAnnotation("android.support.annotation.Annotation2"));
                    } else {
                        assertNotNull(cls.getAnnotation("android.support.annotation.Annotation1"));
                        assertNull(cls.getAnnotation("android.support.annotation.Annotation2"));
                    }

                    return super.visitClassDeclaration(node);
                }

                @Override
                public boolean visitMethodDeclaration(MethodDeclaration node) {
                    ResolvedNode resolved = context.resolve(node);
                    assertNotNull(resolved);
                    assertTrue(resolved.getClass().getName(), resolved instanceof ResolvedMethod);
                    ResolvedMethod method = (ResolvedMethod) resolved;
                    assertNull(method.getAnnotation("android.support.annotation.Annotation5"));
                    assertNotNull(method.getAnnotation("android.support.annotation.Annotation6"));
                    assertNotNull(method.getParameterAnnotation("android.support.annotation.Annotation3", 0));
                    assertNotNull(method.getParameterAnnotation("android.support.annotation.Annotation4", 1));
                    assertNotNull(method.getParameterAnnotation("android.support.annotation.Annotation3", 1));
                    assertNotNull(method.getParameterAnnotation("android.support.annotation.Annotation5", 2));

                    return super.visitMethodDeclaration(node);
                }
            });
        } finally {
            ExternalAnnotationRepository.set(null);
        }
    }

    public void testMergeParameters() throws Exception {
        try {
            ExternalAnnotationRepository manager = getExternalAnnotations("test.pkg", ""
                    + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<root>\n"
                    + "  <item name=\"test.pkg.Test.Parent void testMethod(int)\">\n"
                    + "    <annotation name=\"android.support.annotation.Annotation1\" />\n"
                    + "  </item>\n"
                    + "  <item name=\"test.pkg.Test.Child void testMethod(int)\">\n"
                    + "    <annotation name=\"android.support.annotation.Annotation2\" />\n"
                    + "  </item>\n"
                    + "</root>\n");
            assertNotNull(manager);
            ExternalAnnotationRepository.set(manager);

            String source = ""
                    + "package test.pkg;\n"
                    + "\n"
                    + "public class Test {\n"
                    + "    public void test(Child child) {\n"
                    + "        child.testMethod(5);\n"
                    + "    }\n"
                    + "\n"
                    + "    public static class Parent {\n"
                    + "        public void testMethod(int parameter) {\n"
                    + "        }\n"
                    + "    }\n"
                    + "\n"
                    + "    public static class Child extends Parent {\n"
                    + "        @Override\n"
                    + "        public void testMethod(int parameter) {\n"
                    + "        }\n"
                    + "    }\n"
                    + "}\n";

            final JavaContext context = LintUtilsTest.parse(source,
                    new File("src/test/pkg/Test.java"));
            assertNotNull(context);
            Node unit = context.getCompilationUnit();
            assertNotNull(unit);
            final AtomicBoolean foundMethod = new AtomicBoolean();
            unit.accept(new ForwardingAstVisitor() {
                @Override
                public boolean visitMethodInvocation(MethodInvocation node) {
                    foundMethod.set(true);
                    assertEquals("testMethod", node.astName().astValue());
                    ResolvedNode resolved = context.resolve(node);
                    assertTrue(resolved instanceof ResolvedMethod);
                    ResolvedMethod method = (ResolvedMethod)resolved;
                    List<ResolvedAnnotation> annotations =
                            Lists.newArrayList(method.getAnnotations());
                    assertEquals(3, annotations.size());
                    Collections.sort(annotations,
                            new Comparator<ResolvedAnnotation>() {
                                @Override
                                public int compare(ResolvedAnnotation a1,
                                        ResolvedAnnotation a2) {
                                    return a1.getName().compareTo(a2.getName());
                                }
                            });
                    assertEquals("android.support.annotation.Annotation1", annotations.get(0).getName());
                    assertEquals("android.support.annotation.Annotation2", annotations.get(1).getName());
                    assertEquals("java.lang.Override", annotations.get(2).getName());
                    return super.visitMethodInvocation(node);
                }
            });
            assertTrue(foundMethod.get());
        } finally {
            ExternalAnnotationRepository.set(null);
        }
    }

    public void testSdkAnnotations() throws Exception {
        ExternalAnnotationRepository manager = getSdkAnnotations();
        if (manager == null) {
            // Can't find it when running from Gradle; ignore for now
            return;
        }
        ResolvedMethod method = createMethod("android.view.LayoutInflater", "android.view.View",
                "createView", "java.lang.String, java.lang.String, android.util.AttributeSet");
        assertNotNull(manager.getAnnotation(method, 2, "android.support.annotation.NonNull"));
    }

    private static ResolvedClass createClass(String name) {
        ResolvedClass mock = mock(ResolvedClass.class);
        when(mock.getName()).thenReturn(name);
        when(mock.getSignature()).thenReturn(name);
        assertTrue(name, name.indexOf('.') != -1);
        ResolvedPackage pkg = createPackage(name.substring(0, name.lastIndexOf('.')));
        when(mock.getPackage()).thenReturn(pkg);
        return mock;
    }

    private static ResolvedMethod createConstructor(String containingClass, String name,
            String parameters) {
        return createMethod(containingClass, null, name, parameters, true);
    }

    public static ResolvedMethod createMethod(String containingClass, String returnType,
            String name, String parameters) {
        return createMethod(containingClass, returnType, name, parameters, false);
    }

    public static ResolvedMethod createMethod(String containingClass, String returnType,
            String name, String parameters, boolean isConstructor) {
        return createMethod(containingClass, returnType, name,
                Splitter.on(',').trimResults().split(parameters), isConstructor);
    }

    public static ResolvedMethod createMethod(String containingClass, String returnType,
            String name, Iterable<String> parameters, boolean isConstructor) {
        ResolvedMethod mock = mock(ResolvedMethod.class);
        when(mock.isConstructor()).thenReturn(isConstructor);
        when(mock.getName()).thenReturn(name);
        if (!isConstructor) {
            DefaultTypeDescriptor typeDescriptor = new DefaultTypeDescriptor(returnType);
            when(mock.getReturnType()).thenReturn(typeDescriptor);
        }
        ResolvedClass cls = createClass(containingClass);
        when(mock.getContainingClass()).thenReturn(cls);
        int index = 0;
        for (String argument : parameters) {
            TypeDescriptor typeDescriptor = new DefaultTypeDescriptor(argument);
            when(mock.getArgumentType(index)).thenReturn(typeDescriptor);
            index++;
        }
        when(mock.getArgumentCount()).thenReturn(index);
        return mock;
    }

    public static ResolvedField createField(String containingClass, String name) {
        ResolvedField mock = mock(ResolvedField.class);
        when(mock.getName()).thenReturn(name);
        ResolvedClass cls = createClass(containingClass);
        when(mock.getContainingClass()).thenReturn(cls);
        return mock;
    }

    public static ResolvedPackage createPackage(String pkgName) {
        ResolvedPackage mock = mock(ResolvedPackage.class);
        when(mock.getName()).thenReturn(pkgName);
        return mock;
    }
}
