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

package com.android.tools.lint.checks;

import com.android.tools.lint.detector.api.Detector;

@SuppressWarnings("javadoc")
public class JavaScriptInterfaceDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new JavaScriptInterfaceDetector();
    }

    public void testOlderSdk() throws Exception {
        assertEquals("No warnings.",
                lintProject(
                        "bytecode/.classpath=>.classpath",
                        "project.properties19=>project.properties",
                        "bytecode/AndroidManifest.xml=>AndroidManifest.xml",
                        "bytecode/AnnotatedObject.java.txt=>src/test/pkg/AnnotatedObject.java",
                        "bytecode/InheritsFromAnnotated.java.txt=>src/test/pkg/InheritsFromAnnotated.java",
                        "bytecode/NonAnnotatedObject.java.txt=>src/test/pkg/NonAnnotatedObject.java",
                        "bytecode/JavaScriptTest.java.txt=>src/test/pkg/JavaScriptTest.java",
                        "bytecode/AnnotatedObject.class.data=>bin/classes/test/pkg/AnnotatedObject.class",
                        "bytecode/InheritsFromAnnotated.class.data=>bin/classes/test/pkg/InheritsFromAnnotated.class",
                        "bytecode/JavaScriptTest.class.data=>bin/classes/test/pkg/JavaScriptTest.class",
                        "bytecode/NonAnnotatedObject.class.data=>bin/classes/test/pkg/NonAnnotatedObject.class"
                ));
    }

    public void test() throws Exception {
        assertEquals(
                "src/test/pkg/JavaScriptTest.java:10: Error: None of the methods in the added interface (NonAnnotatedObject) have been annotated with @android.webkit.JavascriptInterface; they will not be visible in API 17 [JavascriptInterface]\n" +
                "  webview.addJavascriptInterface(new NonAnnotatedObject(), \"myobj\");\n" +
                "          ~~~~~~~~~~~~~~~~~~~~~~\n" +
                "src/test/pkg/JavaScriptTest.java:13: Error: None of the methods in the added interface (NonAnnotatedObject) have been annotated with @android.webkit.JavascriptInterface; they will not be visible in API 17 [JavascriptInterface]\n" +
                "  webview.addJavascriptInterface(o, \"myobj\");\n" +
                "          ~~~~~~~~~~~~~~~~~~~~~~\n" +
                "src/test/pkg/JavaScriptTest.java:20: Error: None of the methods in the added interface (NonAnnotatedObject) have been annotated with @android.webkit.JavascriptInterface; they will not be visible in API 17 [JavascriptInterface]\n" +
                "  webview.addJavascriptInterface(object2, \"myobj\");\n" +
                "          ~~~~~~~~~~~~~~~~~~~~~~\n" +
                "src/test/pkg/JavaScriptTest.java:31: Error: None of the methods in the added interface (NonAnnotatedObject) have been annotated with @android.webkit.JavascriptInterface; they will not be visible in API 17 [JavascriptInterface]\n" +
                "  webview.addJavascriptInterface(t, \"myobj\");\n" +
                "          ~~~~~~~~~~~~~~~~~~~~~~\n" +
                "4 errors, 0 warnings\n",

            lintProject(
                    "bytecode/.classpath=>.classpath",
                    "project.properties19=>project.properties",
                    "bytecode/AndroidManifestTarget17.xml=>AndroidManifest.xml",
                    "bytecode/AnnotatedObject.java.txt=>src/test/pkg/AnnotatedObject.java",
                    "bytecode/InheritsFromAnnotated.java.txt=>src/test/pkg/InheritsFromAnnotated.java",
                    "bytecode/NonAnnotatedObject.java.txt=>src/test/pkg/NonAnnotatedObject.java",
                    "bytecode/JavaScriptTest.java.txt=>src/test/pkg/JavaScriptTest.java",
                    "bytecode/AnnotatedObject.class.data=>bin/classes/test/pkg/AnnotatedObject.class",
                    "bytecode/InheritsFromAnnotated.class.data=>bin/classes/test/pkg/InheritsFromAnnotated.class",
                    "bytecode/JavaScriptTest.class.data=>bin/classes/test/pkg/JavaScriptTest.class",
                    "bytecode/NonAnnotatedObject.class.data=>bin/classes/test/pkg/NonAnnotatedObject.class"
                    ));
    }
}
