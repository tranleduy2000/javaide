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
public class SharedPrefsDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new SharedPrefsDetector();
    }

    public void test() throws Exception {
        assertEquals(
            "src/test/pkg/SharedPrefsTest.java:54: Warning: SharedPreferences.edit() without a corresponding commit() or apply() call [CommitPrefEdits]\n" +
            "        SharedPreferences.Editor editor = preferences.edit();\n" +
            "                                          ~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/SharedPrefsTest.java:62: Warning: SharedPreferences.edit() without a corresponding commit() or apply() call [CommitPrefEdits]\n" +
            "        SharedPreferences.Editor editor = preferences.edit();\n" +
            "                                          ~~~~~~~~~~~~~~~~~~\n" +
            "0 errors, 2 warnings\n" +
            "",

            lintProject("src/test/pkg/SharedPrefsTest.java.txt=>" +
                    "src/test/pkg/SharedPrefsTest.java"));
    }

    public void test2() throws Exception {
        // Regression test 1 for http://code.google.com/p/android/issues/detail?id=34322
        assertEquals(
            "src/test/pkg/SharedPrefsTest2.java:13: Warning: SharedPreferences.edit() without a corresponding commit() or apply() call [CommitPrefEdits]\n" +
            "        SharedPreferences.Editor editor = preferences.edit();\n" +
            "                                          ~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/SharedPrefsTest2.java:17: Warning: SharedPreferences.edit() without a corresponding commit() or apply() call [CommitPrefEdits]\n" +
            "        Editor editor = preferences.edit();\n" +
            "                        ~~~~~~~~~~~~~~~~~~\n" +
            "0 errors, 2 warnings\n",

            lintProject("src/test/pkg/SharedPrefsTest2.java.txt=>" +
                    "src/test/pkg/SharedPrefsTest2.java"));
    }

    public void test3() throws Exception {
        // Regression test 2 for http://code.google.com/p/android/issues/detail?id=34322
        assertEquals(
            "src/test/pkg/SharedPrefsTest3.java:13: Warning: SharedPreferences.edit() without a corresponding commit() or apply() call [CommitPrefEdits]\n" +
            "        Editor editor = preferences.edit();\n" +
            "                        ~~~~~~~~~~~~~~~~~~\n" +
            "0 errors, 1 warnings\n",

            lintProject("src/test/pkg/SharedPrefsTest3.java.txt=>" +
                    "src/test/pkg/SharedPrefsTest3.java"));
    }

    public void test4() throws Exception {
        // Regression test 3 for http://code.google.com/p/android/issues/detail?id=34322
        assertEquals(""
            + "src/test/pkg/SharedPrefsTest4.java:13: Warning: SharedPreferences.edit() without a corresponding commit() or apply() call [CommitPrefEdits]\n"
            + "        Editor editor = preferences.edit();\n"
            + "                        ~~~~~~~~~~~~~~~~~~\n"
            + "0 errors, 1 warnings\n",

            lintProject("src/test/pkg/SharedPrefsTest4.java.txt=>" +
                    "src/test/pkg/SharedPrefsTest4.java"));
    }

    public void test5() throws Exception {
        // Check fields too: http://code.google.com/p/android/issues/detail?id=39134
        assertEquals(
            "src/test/pkg/SharedPrefsTest5.java:16: Warning: SharedPreferences.edit() without a corresponding commit() or apply() call [CommitPrefEdits]\n" +
            "        mPreferences.edit().putString(PREF_FOO, \"bar\");\n" +
            "        ~~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/SharedPrefsTest5.java:17: Warning: SharedPreferences.edit() without a corresponding commit() or apply() call [CommitPrefEdits]\n" +
            "        mPreferences.edit().remove(PREF_BAZ).remove(PREF_FOO);\n" +
            "        ~~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/SharedPrefsTest5.java:26: Warning: SharedPreferences.edit() without a corresponding commit() or apply() call [CommitPrefEdits]\n" +
            "        preferences.edit().putString(PREF_FOO, \"bar\");\n" +
            "        ~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/SharedPrefsTest5.java:27: Warning: SharedPreferences.edit() without a corresponding commit() or apply() call [CommitPrefEdits]\n" +
            "        preferences.edit().remove(PREF_BAZ).remove(PREF_FOO);\n" +
            "        ~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/SharedPrefsTest5.java:32: Warning: SharedPreferences.edit() without a corresponding commit() or apply() call [CommitPrefEdits]\n" +
            "        preferences.edit().putString(PREF_FOO, \"bar\");\n" +
            "        ~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/SharedPrefsTest5.java:33: Warning: SharedPreferences.edit() without a corresponding commit() or apply() call [CommitPrefEdits]\n" +
            "        preferences.edit().remove(PREF_BAZ).remove(PREF_FOO);\n" +
            "        ~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/SharedPrefsTest5.java:38: Warning: SharedPreferences.edit() without a corresponding commit() or apply() call [CommitPrefEdits]\n" +
            "        Editor editor = preferences.edit().putString(PREF_FOO, \"bar\");\n" +
            "                        ~~~~~~~~~~~~~~~~~~\n" +
            "0 errors, 7 warnings\n",

            lintProject("src/test/pkg/SharedPrefsTest5.java.txt=>" +
                    "src/test/pkg/SharedPrefsTest5.java"));
    }

    public void test6() throws Exception {
        // Regression test for https://code.google.com/p/android/issues/detail?id=68692
        assertEquals(""
            + "src/test/pkg/SharedPrefsTest7.java:13: Warning: SharedPreferences.edit() without a corresponding commit() or apply() call [CommitPrefEdits]\n"
            + "        settings.edit().putString(MY_PREF_KEY, myPrefValue);\n"
            + "        ~~~~~~~~~~~~~~~\n"
            + "0 errors, 1 warnings\n",

            lintProject("src/test/pkg/SharedPrefsTest7.java.txt=>" +
                    "src/test/pkg/SharedPrefsTest7.java"));
    }

    public void test7() throws Exception {
        assertEquals("No warnings.", // minSdk < 9: no warnings

                lintProject("src/test/pkg/SharedPrefsTest8.java.txt=>" +
                        "src/test/pkg/SharedPrefsTest8.java"));
    }

    public void test8() throws Exception {
        assertEquals(""
            + "src/test/pkg/SharedPrefsTest8.java:11: Warning: Consider using apply() instead; commit writes its data to persistent storage immediately, whereas apply will handle it in the background [CommitPrefEdits]\n"
            + "        editor.commit();\n"
            + "        ~~~~~~~~~~~~~~~\n"
            + "0 errors, 1 warnings\n",

            lintProject(
                    "apicheck/minsdk11.xml=>AndroidManifest.xml",
                    "src/test/pkg/SharedPrefsTest8.java.txt=>src/test/pkg/SharedPrefsTest8.java"));
    }
}
