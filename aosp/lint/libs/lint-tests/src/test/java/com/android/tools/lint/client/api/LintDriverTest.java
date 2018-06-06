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

package com.android.tools.lint.client.api;

import com.android.annotations.NonNull;
import com.android.tools.lint.checks.AbstractCheckTest;
import com.android.tools.lint.checks.AccessibilityDetector;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Project;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("javadoc")
public class LintDriverTest extends AbstractCheckTest {
    @SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
    public void testClassEntryCompare() throws Exception {
        ClassEntry c0 = new ClassEntry(new File("/a1/Foo.class"), null, null, null);
        ClassEntry c1 = new ClassEntry(new File("/a1/Foo.clazz"), null, null, null);
        ClassEntry c2 = new ClassEntry(new File("/a1/Foo$Inner1.class"), null, null, null);
        ClassEntry c3 = new ClassEntry(new File("/a1/Foo$Inner1$Inner.class"), null, null, null);
        ClassEntry c4 = new ClassEntry(new File("/a2/Foo$Inner2.clas"), null, null, null);
        ClassEntry c5 = new ClassEntry(new File("/a2/Foo$Inner2.class"), null, null, null);

        List<ClassEntry> expected = Arrays.asList(c0, c1, c2, c3, c4, c5);
        List<ClassEntry> list = new ArrayList<ClassEntry>(expected);
        Collections.sort(list);
        assertEquals(list, list);

        List<ClassEntry> list2 = Arrays.asList(c5, c4, c3, c2, c1, c0);
        Collections.sort(list2);
        assertEquals(expected, list2);

        List<ClassEntry> list3 = Arrays.asList(c3, c0, c1, c5, c2, c4);
        Collections.sort(list3);
        assertEquals(expected, list3);
    }

    @SuppressWarnings("ConstantConditions")
    public void testClassEntryCompareContract() throws Exception {
        ClassEntry c0 = new ClassEntry(new File("abcde"), null, null, null);
        ClassEntry c1 = new ClassEntry(new File("abcde"), null, null, null);
        assertTrue(c0.compareTo(c1) <= 0);
        assertTrue(c1.compareTo(c0) <= 0);
    }

    public void testMissingResourceDirectory() throws Exception {
        assertEquals("No warnings.", lintProject("res/layout/layout1.xml"));
    }

    @Override
    protected TestLintClient createClient() {
        return new TestLintClient() {
            private List<File> mResources;

            @NonNull
            @Override
            public List<File> getResourceFolders(@NonNull Project project) {
                if (mResources == null) {
                    mResources = Lists.newArrayList(super.getResourceFolders(project));
                    mResources.add(new File("bogus"));
                }
                return mResources;
            }
        };
    }

    @Override
    protected Detector getDetector() {
        return new AccessibilityDetector();
    }
}
