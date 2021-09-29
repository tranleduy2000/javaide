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
public class FragmentDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new FragmentDetector();
    }

    public void test() throws Exception {
        assertEquals(""
            + "src/test/pkg/FragmentTest.java:10: Error: This fragment class should be public (test.pkg.FragmentTest.Fragment1) [ValidFragment]\n"
            + " private static class Fragment1 extends Fragment {\n"
            + "                      ~~~~~~~~~\n"
            + "src/test/pkg/FragmentTest.java:15: Error: This fragment inner class should be static (test.pkg.FragmentTest.Fragment2) [ValidFragment]\n"
            + " public class Fragment2 extends Fragment {\n"
            + "              ~~~~~~~~~\n"
            + "src/test/pkg/FragmentTest.java:21: Error: The default constructor must be public [ValidFragment]\n"
            + "  private Fragment3() {\n"
            + "          ~~~~~~~~~~~\n"
            + "src/test/pkg/FragmentTest.java:26: Error: This fragment should provide a default constructor (a public constructor with no arguments) (test.pkg.FragmentTest.Fragment4) [ValidFragment]\n"
            + " public static class Fragment4 extends Fragment {\n"
            + "                     ~~~~~~~~~\n"
            + "src/test/pkg/FragmentTest.java:27: Error: Avoid non-default constructors in fragments: use a default constructor plus Fragment#setArguments(Bundle) instead [ValidFragment]\n"
            + "  private Fragment4(int dummy) {\n"
            + "          ~~~~~~~~~~~~~~~~~~~~\n"
            + "src/test/pkg/FragmentTest.java:36: Error: Avoid non-default constructors in fragments: use a default constructor plus Fragment#setArguments(Bundle) instead [ValidFragment]\n"
            + "  public Fragment5(int dummy) {\n"
            + "         ~~~~~~~~~~~~~~~~~~~~\n"
            + "6 errors, 0 warnings\n",

            lintProject(
                "bytecode/FragmentTest$Fragment1.class.data=>bin/classes/test/pkg/FragmentTest$Fragment1.class",
                "bytecode/FragmentTest$Fragment2.class.data=>bin/classes/test/pkg/FragmentTest$Fragment2.class",
                "bytecode/FragmentTest$Fragment3.class.data=>bin/classes/test/pkg/FragmentTest$Fragment3.class",
                "bytecode/FragmentTest$Fragment4.class.data=>bin/classes/test/pkg/FragmentTest$Fragment4.class",
                "bytecode/FragmentTest$Fragment5.class.data=>bin/classes/test/pkg/FragmentTest$Fragment5.class",
                "bytecode/FragmentTest$Fragment6.class.data=>bin/classes/test/pkg/FragmentTest$Fragment6.class",
                "bytecode/FragmentTest$NotAFragment.class.data=>bin/classes/test/pkg/FragmentTest$NotAFragment.class",
                "bytecode/FragmentTest.java.txt=>src/test/pkg/FragmentTest.java"));
    }
}
