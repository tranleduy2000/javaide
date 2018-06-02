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

package com.android.sdklib.util;

import junit.framework.TestCase;


public class LineUtilTest extends TestCase {

    public void testReflowLine() {
        boolean gotNpe = false;
        try {
            LineUtil.reflowLine(null);
        } catch(NullPointerException e) {
            gotNpe = true;
        } finally {
            assertTrue(gotNpe);
        }

        assertEquals("", LineUtil.reflowLine(""));

        assertEquals("1234567", LineUtil.reflowLine("1234567"));

        assertEquals(
                "-- verbose, -v: This description for this flag fits in exactly 78 characters.",
                LineUtil.reflowLine("-- verbose, -v: This description for this flag fits in exactly 78 characters."));

        assertEquals(
                "--verbose, -v  :   This description for this flag fits in more than 78\n" +
                "                   characters and needs to wrap up at the colon.",
                LineUtil.reflowLine("--verbose, -v  :   This description for this flag fits in more than 78 characters and needs to wrap up at the colon."));

        assertEquals(
                "If the line needs to wrap but there's no colon marker, the line will just wrap\n" +
                "    with 4 spaces.",
                LineUtil.reflowLine("If the line needs to wrap but there's no colon marker, the line will just wrap with 4 spaces."));

        assertEquals(
                "--blah: More than 78 characters and lots of\n" +
                "        spaces.   ",
                LineUtil.reflowLine("--blah: More than 78 characters and lots of                                spaces.   "));

        assertEquals(
                "In this case the colon is at the very end of the string and it's not going to\n" +
                "    wrap as expected:",
                LineUtil.reflowLine("In this case the colon is at the very end of the string and it's not going to wrap as expected:"));

        assertEquals(
                "--flag:In-this-case-there-is-no-whitespace-and-wrapping-will-cut-just-at-the-7\n" +
                "       8-mark.",
                LineUtil.reflowLine("--flag:In-this-case-there-is-no-whitespace-and-wrapping-will-cut-just-at-the-78-mark."));

        assertEquals(
                "Desc: This line is split in 2.\n" +
                "      The second line must align at the colon and yet still wrap as expected\n" +
                "      if it doesn't fit properly.\n" +
                "      The end.",
                LineUtil.reflowLine("Desc: This line is split in 2.\nThe second line must align at the colon and yet still wrap as expected if it doesn't fit properly.\nThe end."));

        assertEquals(
                "Desc: This line is split in 2\n" +
                "      even though it doesn't need to wrap.",
                LineUtil.reflowLine("Desc: This line is split in 2\n\n\n\n\n\neven though it doesn't need to wrap."));

    }
}
