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

public class CallSuperDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new CallSuperDetector();
    }

    public void testCallSuper() throws Exception {
        assertEquals(""
                + "src/test/pkg/CallSuperTest.java:11: Error: Overriding method should call super.test1 [MissingSuperCall]\n"
                + "        protected void test1() { // ERROR\n"
                + "                       ~~~~~~~\n"
                + "src/test/pkg/CallSuperTest.java:14: Error: Overriding method should call super.test2 [MissingSuperCall]\n"
                + "        protected void test2() { // ERROR\n"
                + "                       ~~~~~~~\n"
                + "src/test/pkg/CallSuperTest.java:17: Error: Overriding method should call super.test3 [MissingSuperCall]\n"
                + "        protected void test3() { // ERROR\n"
                + "                       ~~~~~~~\n"
                + "src/test/pkg/CallSuperTest.java:20: Error: Overriding method should call super.test4 [MissingSuperCall]\n"
                + "        protected void test4(int arg) { // ERROR\n"
                + "                       ~~~~~~~~~~~~~~\n"
                + "src/test/pkg/CallSuperTest.java:26: Error: Overriding method should call super.test5 [MissingSuperCall]\n"
                + "        protected void test5(int arg1, boolean arg2, Map<List<String>,?> arg3,  // ERROR\n"
                + "                       ^\n"
                + "src/test/pkg/CallSuperTest.java:30: Error: Overriding method should call super.test5 [MissingSuperCall]\n"
                + "        protected void test5() { // ERROR\n"
                + "                       ~~~~~~~\n"
                + "6 errors, 0 warnings\n",

                lintProject("src/test/pkg/CallSuperTest.java.txt=>src/test/pkg/CallSuperTest.java",
                        "src/android/support/annotation/CallSuper.java.txt=>src/android/support/annotation/CallSuper.java"));
    }

    @SuppressWarnings("ClassNameDiffersFromFileName")
    public void testCallSuperIndirect() throws Exception {
        // Ensure that when the @CallSuper is on an indirect super method,
        // we correctly check that you call the direct super method, not the ancestor.
        //
        // Regression test for
        //    https://code.google.com/p/android/issues/detail?id=174964
        assertEquals("No warnings.",
            lintProject(
                java("src/test/pkg/CallSuperTest.java", ""
                + "package test.pkg;\n"
                + "\n"
                + "import android.support.annotation.CallSuper;\n"
                + "\n"
                + "import java.util.List;\n"
                + "import java.util.Map;\n"
                + "\n"
                + "@SuppressWarnings(\"UnusedDeclaration\")\n"
                + "public class CallSuperTest {\n"
                + "    private static class Child extends Parent {\n"
                + "        @Override\n"
                + "        protected void test1() {\n"
                + "            super.test1();\n"
                + "        }\n"
                + "    }\n"
                + "\n"
                + "    private static class Parent extends ParentParent {\n"
                + "        @Override\n"
                + "        protected void test1() {\n"
                + "            super.test1();\n"
                + "        }\n"
                + "    }\n"
                + "\n"
                + "    private static class ParentParent extends ParentParentParent {\n"
                + "        @CallSuper\n"
                + "        protected void test1() {\n"
                + "        }\n"
                + "    }\n"
                + "\n"
                + "    private static class ParentParentParent {\n"
                + "\n"
                + "    }\n"
                + "}\n"),
                copy("src/android/support/annotation/CallSuper.java.txt",
                        "src/android/support/annotation/CallSuper.java")));
    }

    public void testDetachFromWindow() throws Exception {
        assertEquals(""
                + "src/test/pkg/DetachedFromWindow.java:7: Error: Overriding method should call super.onDetachedFromWindow [MissingSuperCall]\n"
                + "        protected void onDetachedFromWindow() {\n"
                + "                       ~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/DetachedFromWindow.java:26: Error: Overriding method should call super.onDetachedFromWindow [MissingSuperCall]\n"
                + "        protected void onDetachedFromWindow() {\n"
                + "                       ~~~~~~~~~~~~~~~~~~~~~~\n"
                + "2 errors, 0 warnings\n",

                lintProject("src/test/pkg/DetachedFromWindow.java.txt=>" +
                        "src/test/pkg/DetachedFromWindow.java"));
    }

    public void testWatchFaceVisibility() throws Exception {
        assertEquals(""
                + "src/test/pkg/WatchFaceTest.java:9: Error: Overriding method should call super.onVisibilityChanged [MissingSuperCall]\n"
                + "        public void onVisibilityChanged(boolean visible) { // ERROR: Missing super call\n"
                + "                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings\n",

                lintProject(
                        "src/test/pkg/WatchFaceTest.java.txt=>src/test/pkg/WatchFaceTest.java",
                        "stubs/WatchFaceService.java.txt=>src/android/support/wearable/watchface/WatchFaceService.java",
                        "stubs/CanvasWatchFaceService.java.txt=>src/android/support/wearable/watchface/CanvasWatchFaceService.java"
                ));
    }
}
