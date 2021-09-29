/*
 * Copyright (C) 2012 The Android Open Source Project
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
public class ViewConstructorDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new ViewConstructorDetector();
    }

    public void test() throws Exception {
        assertEquals(""
            + "src/test/bytecode/CustomView1.java:5: Warning: Custom view CustomView1 is missing constructor used by tools: (Context) or (Context,AttributeSet) or (Context,AttributeSet,int) [ViewConstructor]\n"
            + "public class CustomView1 extends View {\n"
            + "             ~~~~~~~~~~~\n"
            + "src/test/bytecode/CustomView2.java:7: Warning: Custom view CustomView2 is missing constructor used by tools: (Context) or (Context,AttributeSet) or (Context,AttributeSet,int) [ViewConstructor]\n"
            + "public class CustomView2 extends Button {\n"
            + "             ~~~~~~~~~~~\n"
            + "0 errors, 2 warnings\n",

            lintProject(
                "bytecode/.classpath=>.classpath",
                "bytecode/AndroidManifest.xml=>AndroidManifest.xml",
                "bytecode/CustomView1.java.txt=>src/test/bytecode/CustomView1.java",
                "bytecode/CustomView2.java.txt=>src/test/bytecode/CustomView2.java",
                "bytecode/CustomView3.java.txt=>src/test/bytecode/CustomView3.java",
                "bytecode/CustomView1.class.data=>bin/classes/test/bytecode/CustomView1.class",
                "bytecode/CustomView2.class.data=>bin/classes/test/bytecode/CustomView2.class",
                "bytecode/CustomView3.class.data=>bin/classes/test/bytecode/CustomView3.class"
                ));
    }

    public void testInheritLocal() throws Exception {
        assertEquals(""
            + "src/test/pkg/CustomViewTest.java:5: Warning: Custom view CustomViewTest is missing constructor used by tools: (Context) or (Context,AttributeSet) or (Context,AttributeSet,int) [ViewConstructor]\n"
            + "public class CustomViewTest extends IntermediateCustomV {\n"
            + "             ~~~~~~~~~~~~~~\n"
            + "0 errors, 1 warnings\n",

            lintProject(
                "bytecode/.classpath=>.classpath",
                "bytecode/AndroidManifest.xml=>AndroidManifest.xml",
                "apicheck/Intermediate.java.txt=>src/test/pkg/Intermediate.java",
                "src/test/pkg/CustomViewTest.java.txt=>src/test/pkg/CustomViewTest.java",
                "bytecode/CustomViewTest.class.data=>bin/classes/test/pkg/CustomViewTest.class",
                "apicheck/Intermediate.class.data=>bin/classes/test/pkg/Intermediate.class",
                "apicheck/Intermediate$IntermediateCustomV.class.data=>" +
                        "bin/classes/test/pkg/Intermediate$IntermediateCustomV.class"
                ));
    }

    public void testAbstract() throws Exception {
        assertEquals(
                "No warnings.",

                lintProject(
                        "bytecode/.classpath=>.classpath",
                        "bytecode/AndroidManifest.xml=>AndroidManifest.xml",
                        "bytecode/AbstractCustomView.java.txt=>src/test/bytecode/AbstractCustomView.java",
                        "bytecode/AbstractCustomView.class.data=>bin/classes/test/bytecode/AbstractCustomView.class"
                ));
    }
}
