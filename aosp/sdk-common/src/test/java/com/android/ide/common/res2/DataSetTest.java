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
package com.android.ide.common.res2;

import junit.framework.TestCase;
import static com.android.ide.common.res2.DataSet.isIgnored;
import static java.io.File.separator;

import com.google.common.io.Files;

import java.io.File;

public class DataSetTest extends TestCase {
    public void testIsIgnored() throws Exception {
        assertNull("Environment variable $ANDROID_AAPT_IGNORE should not be set while "
                        + "running test; can interfere with results",
                System.getenv("ANDROID_AAPT_IGNORE"));
        assertFalse(isIgnored(new File("a.")));
        assertFalse(isIgnored(new File("foo")));
        assertFalse(isIgnored(new File("foo" + separator + "bar")));
        assertFalse(isIgnored(new File("foo")));
        assertFalse(isIgnored(new File("foo" + separator + "bar")));
        assertFalse(isIgnored(new File("layout" + separator + "main.xml")));
        assertFalse(isIgnored(new File("res" + separator + "drawable" + separator + "foo.png")));
        assertFalse(isIgnored(new File("")));

        assertTrue(isIgnored(new File(".")));
        assertTrue(isIgnored(new File("..")));
        assertTrue(isIgnored(new File(".git")));
        assertTrue(isIgnored(new File("foo" + separator + ".git")));
        assertTrue(isIgnored(new File(".svn")));
        assertTrue(isIgnored(new File("thumbs.db")));
        assertTrue(isIgnored(new File("Thumbs.db")));
        assertTrue(isIgnored(new File("foo" + separator + "Thumbs.db")));

        // Suffix
        assertTrue(isIgnored(new File("foo~")));
        assertTrue(isIgnored(new File("foo.scc")));
        assertTrue(isIgnored(new File("foo" + separator + "foo.scc")));

        // Prefix
        assertTrue(isIgnored(new File(".test")));
        assertTrue(isIgnored(new File("foo" + separator + ".test")));

        // Don't match on non-directory
        assertFalse(isIgnored(new File("_test")));
        File dir = new File(Files.createTempDir(), "_test");
        assertTrue(dir.mkdirs());
        assertTrue(isIgnored(dir));
    }
}