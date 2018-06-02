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

package com.android.sdklib.internal.repository.packages;

import com.android.sdklib.repository.FullRevision;
import com.android.sdklib.repository.MajorRevision;

import junit.framework.TestCase;

public class MajorRevisionTest extends TestCase {

    public final void testMajorRevision() {
        MajorRevision p = new MajorRevision(5);
        assertEquals(5, p.getMajor());
        assertEquals(FullRevision.IMPLICIT_MINOR_REV, p.getMinor());
        assertEquals(FullRevision.IMPLICIT_MICRO_REV, p.getMicro());
        assertEquals(FullRevision.NOT_A_PREVIEW, p.getPreview());
        assertFalse (p.isPreview());
        assertEquals("5", p.toShortString());
        assertEquals(p, MajorRevision.parseRevision("5"));
        assertEquals("5", p.toString());

        assertEquals(new FullRevision(5, 0, 0, 0), p);
    }

    public final void testParseError() {
        String errorMsg = null;
        try {
            MajorRevision.parseRevision("5.0.0");
            fail("MajorRevision.parseRevision should thrown NumberFormatException");
        } catch (NumberFormatException e) {
            errorMsg = e.getMessage();
        }
        assertEquals("Invalid revision: 5.0.0 -- Micro number not supported", errorMsg);
    }

    public final void testCompareTo() {
        MajorRevision s4 = new MajorRevision(4);
        MajorRevision i4 = new MajorRevision(4);
        FullRevision  g5 = new FullRevision (5, 1, 0, 6);
        MajorRevision y5 = new MajorRevision(5);
        FullRevision  c5 = new FullRevision (5, 1, 0, 6);
        FullRevision  o5 = new FullRevision (5, 0, 0, 7);
        FullRevision  p5 = new FullRevision (5, 1, 0, 0);

        assertEquals(s4, i4);                   // 4.0.0-0 == 4.0.0-0
        assertEquals(g5, c5);                   // 5.1.0-6 == 5.1.0-6

        assertFalse(y5.equals(p5));             // 5.0.0-0 != 5.1.0-0
        assertFalse(g5.equals(p5));             // 5.1.0-6 != 5.1.0-0
        assertTrue (s4.compareTo(i4) == 0);     // 4.0.0-0 == 4.0.0-0
        assertTrue (s4.compareTo(y5)  < 0);     // 4.0.0-0  < 5.0.0-0
        assertTrue (y5.compareTo(y5) == 0);     // 5.0.0-0 == 5.0.0-0
        assertTrue (y5.compareTo(p5)  < 0);     // 5.0.0-0  < 5.1.0-0
        assertTrue (o5.compareTo(y5)  < 0);     // 5.0.0-7  < 5.0.0-0
        assertTrue (p5.compareTo(p5) == 0);     // 5.1.0-0 == 5.1.0-0
        assertTrue (c5.compareTo(p5)  < 0);     // 5.1.0-6  < 5.1.0-0
        assertTrue (p5.compareTo(c5)  > 0);     // 5.1.0-0  > 5.1.0-6
        assertTrue (p5.compareTo(o5)  > 0);     // 5.1.0-0  > 5.0.0-7
        assertTrue (c5.compareTo(o5)  > 0);     // 5.1.0-6  > 5.0.0-7
        assertTrue (o5.compareTo(o5) == 0);     // 5.0.0-7  > 5.0.0-7
    }

}
