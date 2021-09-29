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

package com.android.tools.lint.detector.api;

import junit.framework.TestCase;

public class SeverityTest extends TestCase {
    public void testGetName() {
        assertEquals("ERROR", Severity.ERROR.getName());
        assertEquals("WARNING", Severity.WARNING.getName());
    }

    public void testGetDescription() {
        assertEquals("Error", Severity.ERROR.getDescription());
        assertEquals("Warning", Severity.WARNING.getDescription());
    }

    public void testFromString() {
        assertSame(Severity.ERROR, Severity.fromName("ERROR"));
        assertSame(Severity.ERROR, Severity.fromName("error"));
        assertSame(Severity.ERROR, Severity.fromName("Error"));
        assertSame(Severity.WARNING, Severity.fromName("WARNING"));
        assertSame(Severity.WARNING, Severity.fromName("warning"));
        assertSame(Severity.FATAL, Severity.fromName("FATAL"));
        assertSame(Severity.INFORMATIONAL, Severity.fromName("Informational"));
        assertSame(Severity.IGNORE, Severity.fromName("ignore"));
        assertSame(Severity.IGNORE, Severity.fromName("IGNORE"));
    }

    public void testCompare() {
        assertTrue(Severity.IGNORE.compareTo(Severity.ERROR) > 0);
        assertTrue(Severity.WARNING.compareTo(Severity.ERROR) > 0);
        assertTrue(Severity.ERROR.compareTo(Severity.ERROR) == 0);
        assertTrue(Severity.FATAL.compareTo(Severity.ERROR) < 0);
        assertTrue(Severity.WARNING.compareTo(Severity.ERROR) > 0);
    }
}
