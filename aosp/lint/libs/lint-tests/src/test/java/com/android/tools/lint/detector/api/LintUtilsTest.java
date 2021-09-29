/*
 * Copyright (C) 2011 The Android Open Source Project
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

import static com.android.tools.lint.detector.api.LintUtils.computeResourceName;
import static com.android.tools.lint.detector.api.LintUtils.convertVersion;
import static com.android.tools.lint.detector.api.LintUtils.escapePropertyValue;
import static com.android.tools.lint.detector.api.LintUtils.findSubstring;
import static com.android.tools.lint.detector.api.LintUtils.getFormattedParameters;
import static com.android.tools.lint.detector.api.LintUtils.getLocaleAndRegion;
import static com.android.tools.lint.detector.api.LintUtils.isImported;
import static com.android.tools.lint.detector.api.LintUtils.splitPath;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.model.AndroidProject;
import com.android.builder.model.ApiVersion;
import com.android.sdklib.AndroidVersion;
import com.android.sdklib.IAndroidTarget;
import com.android.tools.lint.EcjParser;
import com.android.tools.lint.LintCliClient;
import com.android.tools.lint.checks.BuiltinIssueRegistry;
import com.android.tools.lint.client.api.JavaParser;
import com.android.tools.lint.client.api.LintDriver;
import com.google.common.collect.Iterables;

import junit.framework.TestCase;

import org.intellij.lang.annotations.Language;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import lombok.ast.Node;

@SuppressWarnings("javadoc")
public class LintUtilsTest extends TestCase {
    public void testPrintList() throws Exception {
        assertEquals("foo, bar, baz",
                LintUtils.formatList(Arrays.asList("foo", "bar", "baz"), 3));
        assertEquals("foo, bar, baz",
                LintUtils.formatList(Arrays.asList("foo", "bar", "baz"), 5));

        assertEquals("foo, bar, baz... (3 more)",
                LintUtils.formatList(
                        Arrays.asList("foo", "bar", "baz", "4", "5", "6"), 3));
        assertEquals("foo... (5 more)",
                LintUtils.formatList(
                        Arrays.asList("foo", "bar", "baz", "4", "5", "6"), 1));
        assertEquals("foo, bar, baz",
                LintUtils.formatList(Arrays.asList("foo", "bar", "baz"), 0));
    }

    public void testEndsWith() throws Exception {
        assertTrue(LintUtils.endsWith("Foo", ""));
        assertTrue(LintUtils.endsWith("Foo", "o"));
        assertTrue(LintUtils.endsWith("Foo", "oo"));
        assertTrue(LintUtils.endsWith("Foo", "Foo"));
        assertTrue(LintUtils.endsWith("Foo", "FOO"));
        assertTrue(LintUtils.endsWith("Foo", "fOO"));

        assertFalse(LintUtils.endsWith("Foo", "f"));
    }

    public void testStartsWith() throws Exception {
        assertTrue(LintUtils.startsWith("FooBar", "Bar", 3));
        assertTrue(LintUtils.startsWith("FooBar", "BAR", 3));
        assertTrue(LintUtils.startsWith("FooBar", "Foo", 0));
        assertFalse(LintUtils.startsWith("FooBar", "Foo", 2));
    }

    public void testIsXmlFile() throws Exception {
        assertTrue(LintUtils.isXmlFile(new File("foo.xml")));
        assertTrue(LintUtils.isXmlFile(new File("foo.Xml")));
        assertTrue(LintUtils.isXmlFile(new File("foo.XML")));

        assertFalse(LintUtils.isXmlFile(new File("foo.png")));
        assertFalse(LintUtils.isXmlFile(new File("xml")));
        assertFalse(LintUtils.isXmlFile(new File("xml.png")));
    }

    public void testGetBasename() throws Exception {
        assertEquals("foo", LintUtils.getBaseName("foo.png"));
        assertEquals("foo", LintUtils.getBaseName("foo.9.png"));
        assertEquals(".foo", LintUtils.getBaseName(".foo"));
    }

    public void testEditDistance() {
        assertEquals(0, LintUtils.editDistance("kitten", "kitten"));

        // editing kitten to sitting has edit distance 3:
        //   replace k with s
        //   replace e with i
        //   append g
        assertEquals(3, LintUtils.editDistance("kitten", "sitting"));

        assertEquals(3, LintUtils.editDistance("saturday", "sunday"));
        assertEquals(1, LintUtils.editDistance("button", "bitton"));
        assertEquals(6, LintUtils.editDistance("radiobutton", "bitton"));
    }

    public void testSplitPath() throws Exception {
        assertTrue(Arrays.equals(new String[] { "/foo", "/bar", "/baz" },
                Iterables.toArray(splitPath("/foo:/bar:/baz"), String.class)));

        assertTrue(Arrays.equals(new String[] { "/foo", "/bar" },
                Iterables.toArray(splitPath("/foo;/bar"), String.class)));

        assertTrue(Arrays.equals(new String[] { "/foo", "/bar:baz" },
                Iterables.toArray(splitPath("/foo;/bar:baz"), String.class)));

        assertTrue(Arrays.equals(new String[] { "\\foo\\bar", "\\bar\\foo" },
                Iterables.toArray(splitPath("\\foo\\bar;\\bar\\foo"), String.class)));

        assertTrue(Arrays.equals(new String[] { "${sdk.dir}\\foo\\bar", "\\bar\\foo" },
                Iterables.toArray(splitPath("${sdk.dir}\\foo\\bar;\\bar\\foo"),
                        String.class)));

        assertTrue(Arrays.equals(new String[] { "${sdk.dir}/foo/bar", "/bar/foo" },
                Iterables.toArray(splitPath("${sdk.dir}/foo/bar:/bar/foo"),
                        String.class)));

        assertTrue(Arrays.equals(new String[] { "C:\\foo", "/bar" },
                Iterables.toArray(splitPath("C:\\foo:/bar"), String.class)));
    }

    public void testCommonParen1() {
        assertEquals(new File("/a"), (LintUtils.getCommonParent(
                new File("/a/b/c/d/e"), new File("/a/c"))));
        assertEquals(new File("/a"), (LintUtils.getCommonParent(
                new File("/a/c"), new File("/a/b/c/d/e"))));

        assertEquals(new File("/"), LintUtils.getCommonParent(
                new File("/foo/bar"), new File("/bar/baz")));
        assertEquals(new File("/"), LintUtils.getCommonParent(
                new File("/foo/bar"), new File("/")));
        assertNull(LintUtils.getCommonParent(
               new File("C:\\Program Files"), new File("F:\\")));
        assertNull(LintUtils.getCommonParent(
                new File("C:/Program Files"), new File("F:/")));

        assertEquals(new File("/foo/bar/baz"), LintUtils.getCommonParent(
                new File("/foo/bar/baz"), new File("/foo/bar/baz")));
        assertEquals(new File("/foo/bar"), LintUtils.getCommonParent(
                new File("/foo/bar/baz"), new File("/foo/bar")));
        assertEquals(new File("/foo/bar"), LintUtils.getCommonParent(
                new File("/foo/bar/baz"), new File("/foo/bar/foo")));
        assertEquals(new File("/foo"), LintUtils.getCommonParent(
                new File("/foo/bar"), new File("/foo/baz")));
        assertEquals(new File("/foo"), LintUtils.getCommonParent(
                new File("/foo/bar"), new File("/foo/baz")));
        assertEquals(new File("/foo/bar"), LintUtils.getCommonParent(
                new File("/foo/bar"), new File("/foo/bar/baz")));
    }

    public void testCommonParent2() {
        assertEquals(new File("/"), LintUtils.getCommonParent(
                Arrays.asList(new File("/foo/bar"), new File("/bar/baz"))));
        assertEquals(new File("/"), LintUtils.getCommonParent(
                Arrays.asList(new File("/foo/bar"), new File("/"))));
        assertNull(LintUtils.getCommonParent(
                Arrays.asList(new File("C:\\Program Files"), new File("F:\\"))));
        assertNull(LintUtils.getCommonParent(
                Arrays.asList(new File("C:/Program Files"), new File("F:/"))));

        assertEquals(new File("/foo"), LintUtils.getCommonParent(
                Arrays.asList(new File("/foo/bar"), new File("/foo/baz"))));
        assertEquals(new File("/foo"), LintUtils.getCommonParent(
                Arrays.asList(new File("/foo/bar"), new File("/foo/baz"),
                        new File("/foo/baz/f"))));
        assertEquals(new File("/foo/bar"), LintUtils.getCommonParent(
                Arrays.asList(new File("/foo/bar"), new File("/foo/bar/baz"),
                        new File("/foo/bar/foo2/foo3"))));
    }

    public void testStripIdPrefix() throws Exception {
        assertEquals("foo", LintUtils.stripIdPrefix("@+id/foo"));
        assertEquals("foo", LintUtils.stripIdPrefix("@id/foo"));
        assertEquals("foo", LintUtils.stripIdPrefix("foo"));
    }

    public void testIdReferencesMatch() throws Exception {
        assertTrue(LintUtils.idReferencesMatch("@+id/foo", "@+id/foo"));
        assertTrue(LintUtils.idReferencesMatch("@id/foo", "@id/foo"));
        assertTrue(LintUtils.idReferencesMatch("@id/foo", "@+id/foo"));
        assertTrue(LintUtils.idReferencesMatch("@+id/foo", "@id/foo"));

        assertFalse(LintUtils.idReferencesMatch("@+id/foo", "@+id/bar"));
        assertFalse(LintUtils.idReferencesMatch("@id/foo", "@+id/bar"));
        assertFalse(LintUtils.idReferencesMatch("@+id/foo", "@id/bar"));
        assertFalse(LintUtils.idReferencesMatch("@+id/foo", "@+id/bar"));

        assertFalse(LintUtils.idReferencesMatch("@+id/foo", "@+id/foo1"));
        assertFalse(LintUtils.idReferencesMatch("@id/foo", "@id/foo1"));
        assertFalse(LintUtils.idReferencesMatch("@id/foo", "@+id/foo1"));
        assertFalse(LintUtils.idReferencesMatch("@+id/foo", "@id/foo1"));

        assertFalse(LintUtils.idReferencesMatch("@+id/foo1", "@+id/foo"));
        assertFalse(LintUtils.idReferencesMatch("@id/foo1", "@id/foo"));
        assertFalse(LintUtils.idReferencesMatch("@id/foo1", "@+id/foo"));
        assertFalse(LintUtils.idReferencesMatch("@+id/foo1", "@id/foo"));
    }

    private static void checkEncoding(String encoding, boolean writeBom, String lineEnding)
            throws Exception {
        @SuppressWarnings("StringBufferReplaceableByString")
        StringBuilder sb = new StringBuilder();

        // Norwegian extra vowel characters such as "latin small letter a with ring above"
        String value = "\u00e6\u00d8\u00e5";
        String expected = "First line." + lineEnding + "Second line." + lineEnding
                + "Third line." + lineEnding + value + lineEnding;
        sb.append(expected);
        File file = File.createTempFile("getEncodingTest" + encoding + writeBom, ".txt");
        file.deleteOnExit();
        BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
        OutputStreamWriter writer = new OutputStreamWriter(stream, encoding);

        if (writeBom) {
            String normalized = encoding.toLowerCase(Locale.US).replace("-", "_");
            if (normalized.equals("utf_8")) {
                stream.write(0xef);
                stream.write(0xbb);
                stream.write(0xbf);
            } else if (normalized.equals("utf_16")) {
                stream.write(0xfe);
                stream.write(0xff);
            } else if (normalized.equals("utf_16le")) {
                stream.write(0xff);
                stream.write(0xfe);
            } else if (normalized.equals("utf_32")) {
                stream.write(0x0);
                stream.write(0x0);
                stream.write(0xfe);
                stream.write(0xff);
            } else if (normalized.equals("utf_32le")) {
                stream.write(0xff);
                stream.write(0xfe);
                stream.write(0x0);
                stream.write(0x0);
            } else {
                fail("Can't write BOM for encoding " + encoding);
            }
        }
        writer.write(sb.toString());
        writer.close();

        String s = LintUtils.getEncodedString(new LintCliClient(), file);
        assertEquals(expected, s);
    }

    public void testGetEncodedString() throws Exception {
        checkEncoding("utf-8", false /*bom*/, "\n");
        checkEncoding("UTF-8", false /*bom*/, "\n");
        checkEncoding("UTF_16", false /*bom*/, "\n");
        checkEncoding("UTF-16", false /*bom*/, "\n");
        checkEncoding("UTF_16LE", false /*bom*/, "\n");

        // Try BOM's
        checkEncoding("utf-8", true /*bom*/, "\n");
        checkEncoding("UTF-8", true /*bom*/, "\n");
        checkEncoding("UTF_16", true /*bom*/, "\n");
        checkEncoding("UTF-16", true /*bom*/, "\n");
        checkEncoding("UTF_16LE", true /*bom*/, "\n");
        checkEncoding("UTF_32", true /*bom*/, "\n");
        checkEncoding("UTF_32LE", true /*bom*/, "\n");

        // Make sure this works for \r and \r\n as well
        checkEncoding("UTF-16", false /*bom*/, "\r");
        checkEncoding("UTF_16LE", false /*bom*/, "\r");
        checkEncoding("UTF-16", false /*bom*/, "\r\n");
        checkEncoding("UTF_16LE", false /*bom*/, "\r\n");
        checkEncoding("UTF-16", true /*bom*/, "\r");
        checkEncoding("UTF_16LE", true /*bom*/, "\r");
        checkEncoding("UTF_32", true /*bom*/, "\r");
        checkEncoding("UTF_32LE", true /*bom*/, "\r");
        checkEncoding("UTF-16", true /*bom*/, "\r\n");
        checkEncoding("UTF_16LE", true /*bom*/, "\r\n");
        checkEncoding("UTF_32", true /*bom*/, "\r\n");
        checkEncoding("UTF_32LE", true /*bom*/, "\r\n");
    }

    public void testGetLocaleAndRegion() throws Exception {
        assertNull(getLocaleAndRegion(""));
        assertNull(getLocaleAndRegion("values"));
        assertNull(getLocaleAndRegion("values-xlarge-port"));
        assertEquals("en", getLocaleAndRegion("values-en"));
        assertEquals("pt-rPT", getLocaleAndRegion("values-pt-rPT-nokeys"));
        assertEquals("b+pt+PT", getLocaleAndRegion("values-b+pt+PT-nokeys"));
        assertEquals("zh-rCN", getLocaleAndRegion("values-zh-rCN-keyshidden"));
        assertEquals("ms", getLocaleAndRegion("values-ms-keyshidden"));
    }

    public void testIsImported() throws Exception {
        assertFalse(isImported(getCompilationUnit(
                "package foo.bar;\n" +
                "class Foo {\n" +
                "}\n"),
                "android.app.Activity"));

        assertTrue(isImported(getCompilationUnit(
                "package foo.bar;\n" +
                "import foo.bar.*;\n" +
                "import android.app.Activity;\n" +
                "import foo.bar.Baz;\n" +
                "class Foo {\n" +
                "}\n"),
                "android.app.Activity"));

        assertTrue(isImported(getCompilationUnit(
                "package foo.bar;\n" +
                "import android.app.Activity;\n" +
                "class Foo {\n" +
                "}\n"),
                "android.app.Activity"));

        assertTrue(isImported(getCompilationUnit(
                "package foo.bar;\n" +
                "import android.app.*;\n" +
                "class Foo {\n" +
                "}\n"),
                "android.app.Activity"));

        assertFalse(isImported(getCompilationUnit(
                "package foo.bar;\n" +
                "import android.app.*;\n" +
                "import foo.bar.Activity;\n" +
                "class Foo {\n" +
                "}\n"),
                "android.app.Activity"));
    }

    public void testComputeResourceName() {
        assertEquals("", computeResourceName("", ""));
        assertEquals("foo", computeResourceName("", "foo"));
        assertEquals("foo", computeResourceName("foo", ""));
        assertEquals("prefix_name", computeResourceName("prefix_", "name"));
        assertEquals("prefixName", computeResourceName("prefix", "name"));
    }

    public static Node getCompilationUnit(@Language("JAVA") String javaSource) {
        return getCompilationUnit(javaSource, new File("test"));
    }


    public static Node getCompilationUnit(@Language("JAVA") String javaSource, File relativePath) {
        JavaContext context = parse(javaSource, relativePath);
        return context.getCompilationUnit();
    }

    public static JavaContext parse(@Language("JAVA") final String javaSource,
            final File relativePath) {
        File dir = new File("projectDir");
        final File fullPath = new File(dir, relativePath.getPath());
        LintCliClient client = new LintCliClient() {
            @NonNull
            @Override
            public String readFile(@NonNull File file) {
                if (file.getPath().equals(fullPath.getPath())) {
                    return javaSource;
                }
                return super.readFile(file);
            }

            @Nullable
            @Override
            public IAndroidTarget getCompileTarget(@NonNull Project project) {
                IAndroidTarget[] targets = getTargets();
                for (int i = targets.length - 1; i >= 0; i--) {
                    IAndroidTarget target = targets[i];
                    if (target.isPlatform()) {
                        return target;
                    }
                }

                return super.getCompileTarget(project);
            }
        };
        Project project = client.getProject(dir, dir);

        LintDriver driver = new LintDriver(new BuiltinIssueRegistry(),
                new LintCliClient());
        driver.setScope(Scope.JAVA_FILE_SCOPE);
        TestContext context = new TestContext(driver, client, project, javaSource, fullPath);
        JavaParser parser = new EcjParser(client, project);
        parser.prepareJavaParse(Collections.<JavaContext>singletonList(context));
        Node compilationUnit = parser.parseJava(context);
        assertNotNull(javaSource, compilationUnit);
        context.setCompilationUnit(compilationUnit);
        return context;
    }

    public void testConvertVersion() {
        assertEquals(new AndroidVersion(5, null), convertVersion(new DefaultApiVersion(5, null),
                null));
        assertEquals(new AndroidVersion(19, null), convertVersion(new DefaultApiVersion(19, null),
                null));
        //noinspection SpellCheckingInspection
        assertEquals(new AndroidVersion(18, "KITKAT"), // a preview platform API level is not final
                convertVersion(new DefaultApiVersion(0, "KITKAT"),
                null));
    }

    public void testIsModelOlderThan() throws Exception {
        AndroidProject project = mock(AndroidProject.class);
        when(project.getModelVersion()).thenReturn("0.10.4");

        assertTrue(LintUtils.isModelOlderThan(project, 0, 10, 5));
        assertTrue(LintUtils.isModelOlderThan(project, 0, 11, 0));
        assertTrue(LintUtils.isModelOlderThan(project, 0, 11, 4));
        assertTrue(LintUtils.isModelOlderThan(project, 1, 0, 0));

        project = mock(AndroidProject.class);
        when(project.getModelVersion()).thenReturn("0.11.0");

        assertTrue(LintUtils.isModelOlderThan(project, 1, 0, 0));
        assertFalse(LintUtils.isModelOlderThan(project, 0, 11, 0));
        assertFalse(LintUtils.isModelOlderThan(project, 0, 10, 4));

        project = mock(AndroidProject.class);
        when(project.getModelVersion()).thenReturn("0.11.5");

        assertTrue(LintUtils.isModelOlderThan(project, 1, 0, 0));
        assertFalse(LintUtils.isModelOlderThan(project, 0, 11, 0));

        project = mock(AndroidProject.class);
        when(project.getModelVersion()).thenReturn("1.0.0");

        assertTrue(LintUtils.isModelOlderThan(project, 1, 0, 1));
        assertFalse(LintUtils.isModelOlderThan(project, 1, 0, 0));
        assertFalse(LintUtils.isModelOlderThan(project, 0, 11, 0));
    }

    private static final class DefaultApiVersion implements ApiVersion {
        private final int mApiLevel;
        private final String mCodename;

        public DefaultApiVersion(int apiLevel, @Nullable String codename) {
            mApiLevel = apiLevel;
            mCodename = codename;
        }

        @Override
        public int getApiLevel() {
            return mApiLevel;
        }

        @Nullable
        @Override
        public String getCodename() {
            return mCodename;
        }

        @NonNull
        @Override
        public String getApiString() {
            fail("Not needed in this test");
            return "<invalid>";
        }
    }

    public void testFindSubstring() {
       assertEquals("foo", findSubstring("foo", null, null));
       assertEquals("foo", findSubstring("foo  ", null, "  "));
       assertEquals("foo", findSubstring("  foo", "  ", null));
       assertEquals("foo", findSubstring("[foo]", "[", "]"));
    }

    public void testGetFormattedParameters() {
        assertEquals(Arrays.asList("foo","bar"),
                getFormattedParameters("Prefix %1$s Divider %2$s Suffix",
                        "Prefix foo Divider bar Suffix"));
    }

    public void testEscapePropertyValue() throws Exception {
        assertEquals("foo", escapePropertyValue("foo"));
        assertEquals("\\  foo  ", escapePropertyValue("  foo  "));
        assertEquals("c\\:/foo/bar", escapePropertyValue("c:/foo/bar"));
        assertEquals("\\!\\#\\:\\\\a\\\\b\\\\c", escapePropertyValue("!#:\\a\\b\\c"));
        assertEquals(
                "foofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoo\\#foofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoo",
                escapePropertyValue("foofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoo#foofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoofoo"));
    }

    private static class TestContext extends JavaContext {
        private final String mJavaSource;
        public TestContext(LintDriver driver, LintCliClient client, Project project,
                String javaSource, File file) {
            //noinspection ConstantConditions
            super(driver, project,
                    null, file, client.getJavaParser(null));

            mJavaSource = javaSource;
        }

        @Override
        @Nullable
        public String getContents() {
            return mJavaSource;
        }
    }
}
