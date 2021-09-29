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

package com.android.tools.lint;

import static com.android.tools.lint.Reporter.encodeUrl;
import static com.android.tools.lint.Reporter.getRelativePath;

import junit.framework.TestCase;

import java.io.File;

public class ReporterTest extends TestCase {
    private static File file(String path) {
        return new File(path.replace('/', File.separatorChar));
    }

    public void testEncodeUrl() {
        assertEquals("a/b/c", encodeUrl("a/b/c"));
        assertEquals("a/b/c", encodeUrl("a\\b\\c"));
        assertEquals("a/b/c/%24%26%2B%2C%3A%3B%3D%3F%40/foo+bar%25/d",
                encodeUrl("a/b/c/$&+,:;=?@/foo bar%/d"));
        assertEquals("a/%28b%29/d", encodeUrl("a/(b)/d"));
        assertEquals("a/b+c/d", encodeUrl("a/b c/d")); // + or %20
    }

    public void testRelative() {
        assertEquals(file("../../d/e/f").getPath(),
                getRelativePath(file("a/b/c"), file("d/e/f")));
        assertEquals(file("../d/e/f").getPath(),
                getRelativePath(file("a/b/c"), file("a/d/e/f")));
        assertEquals(file("../d/e/f").getPath(),
                getRelativePath(file("1/2/3/a/b/c"), file("1/2/3/a/d/e/f")));
        assertEquals(file("c").getPath(),
                getRelativePath(file("a/b/c"), file("a/b/c")));
        assertEquals(file("../../e").getPath(),
                getRelativePath(file("a/b/c/d/e/f"), file("a/b/c/e")));
        assertEquals(file("d/e/f").getPath(),
                getRelativePath(file("a/b/c/e"), file("a/b/c/d/e/f")));
    }
}
