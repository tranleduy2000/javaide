/*
 * Copyright (C) 2014 The Android Open Source Project
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

public class AddJavascriptInterfaceDetectorTest extends AbstractCheckTest {

    @Override
    protected Detector getDetector() {
        return new AddJavascriptInterfaceDetector();
    }

    public void test() throws Exception {
        assertEquals(""
            + "src/test/pkg/AddJavascriptInterfaceTest.java:16: Warning: WebView.addJavascriptInterface should not be called with minSdkVersion < 17 for security reasons: JavaScript can use reflection to manipulate application [AddJavascriptInterface]\n"
            + "            webView.addJavascriptInterface(object, string);\n"
            + "                    ~~~~~~~~~~~~~~~~~~~~~~\n"
            + "src/test/pkg/AddJavascriptInterfaceTest.java:23: Warning: WebView.addJavascriptInterface should not be called with minSdkVersion < 17 for security reasons: JavaScript can use reflection to manipulate application [AddJavascriptInterface]\n"
            + "            webView.addJavascriptInterface(object, string);\n"
            + "                    ~~~~~~~~~~~~~~~~~~~~~~\n"
            + "0 errors, 2 warnings\n",

            lintProject(
                "bytecode/.classpath=>.classpath",
                "bytecode/AndroidManifest.xml=>AndroidManifest.xml",
                "bytecode/AddJavascriptInterfaceTest.java.txt=>src/test/pkg/AddJavascriptInterfaceTest.java",
                "bytecode/AddJavascriptInterfaceTest.class.data=>bin/classes/test/pkg/AddJavascriptInterfaceTest.class",
                "bytecode/AddJavascriptInterfaceTest$CallAddJavascriptInterfaceOnWebView.class.data=>"
                        + "bin/classes/test/pkg/AddJavascriptInterfaceTest$CallAddJavascriptInterfaceOnWebView.class",
                "bytecode/AddJavascriptInterfaceTest$WebViewChild.class.data=>"
                        + "bin/classes/test/pkg/AddJavascriptInterfaceTest$WebViewChild.class",
                "bytecode/AddJavascriptInterfaceTest$CallAddJavascriptInterfaceOnWebViewChild.class.data=>"
                        + "bin/classes/test/pkg/AddJavascriptInterfaceTest$CallAddJavascriptInterfaceOnWebViewChild.class",
                "bytecode/AddJavascriptInterfaceTest$NonWebView.class.data=>"
                        + "bin/classes/test/pkg/AddJavascriptInterfaceTest$NonWebView.class",
                "bytecode/AddJavascriptInterfaceTest$CallAddJavascriptInterfaceOnNonWebView.class.data=>"
                        + "bin/classes/test/pkg/AddJavascriptInterfaceTest$CallAddJavascriptInterfaceOnNonWebView.class"
            ));
    }

    public void testNoWarningWhenMinSdkAt17() throws Exception {
        assertEquals(
            "No warnings.",
            lintProject(
                "bytecode/.classpath=>.classpath",
                "bytecode/AndroidManifestMinSdk17.xml=>AndroidManifest.xml",
                "bytecode/AddJavascriptInterfaceTest.java.txt=>src/test/pkg/AddJavascriptInterfaceTest.java",
                "bytecode/AddJavascriptInterfaceTest.class.data=>bin/classes/test/pkg/AddJavascriptInterfaceTest.class",
                "bytecode/AddJavascriptInterfaceTest$CallAddJavascriptInterfaceOnWebView.class.data=>"
                        + "bin/classes/test/pkg/AddJavascriptInterfaceTest$CallAddJavascriptInterfaceOnWebView.class"
            ));
    }
}
