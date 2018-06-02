/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.ide.common.resources.configuration;

import com.android.resources.ScreenSize;

import junit.framework.TestCase;

public class ScreenSizeQualifierTest extends TestCase {

    private ScreenSizeQualifier ssq;
    private FolderConfiguration config;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ssq = new ScreenSizeQualifier();
        config = new FolderConfiguration();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        ssq = null;
        config = null;
    }

    public void testSmall() {
        assertEquals(true, ssq.checkAndSet("small", config)); //$NON-NLS-1$
        assertTrue(config.getScreenSizeQualifier() != null);
        assertEquals(ScreenSize.SMALL, config.getScreenSizeQualifier().getValue());
        assertEquals("small", config.getScreenSizeQualifier().toString()); //$NON-NLS-1$
    }

    public void testNormal() {
        assertEquals(true, ssq.checkAndSet("normal", config)); //$NON-NLS-1$
        assertTrue(config.getScreenSizeQualifier() != null);
        assertEquals(ScreenSize.NORMAL, config.getScreenSizeQualifier().getValue());
        assertEquals("normal", config.getScreenSizeQualifier().toString()); //$NON-NLS-1$
    }

    public void testLarge() {
        assertEquals(true, ssq.checkAndSet("large", config)); //$NON-NLS-1$
        assertTrue(config.getScreenSizeQualifier() != null);
        assertEquals(ScreenSize.LARGE, config.getScreenSizeQualifier().getValue());
        assertEquals("large", config.getScreenSizeQualifier().toString()); //$NON-NLS-1$
    }

    public void testXLarge() {
        assertEquals(true, ssq.checkAndSet("xlarge", config)); //$NON-NLS-1$
        assertTrue(config.getScreenSizeQualifier() != null);
        assertEquals(ScreenSize.XLARGE, config.getScreenSizeQualifier().getValue());
        assertEquals("xlarge", config.getScreenSizeQualifier().toString()); //$NON-NLS-1$
    }

    public void testIsMatchFor() {
        // create qualifiers for small, normal, large and xlarge sizes.
        ScreenSizeQualifier smallQ = new ScreenSizeQualifier(ScreenSize.SMALL);
        ScreenSizeQualifier normalQ = new ScreenSizeQualifier(ScreenSize.NORMAL);
        ScreenSizeQualifier largeQ = new ScreenSizeQualifier(ScreenSize.LARGE);
        ScreenSizeQualifier xlargeQ = new ScreenSizeQualifier(ScreenSize.XLARGE);

        // test that every qualifier is a match for itself.
        assertTrue(smallQ.isMatchFor(smallQ));
        assertTrue(normalQ.isMatchFor(normalQ));
        assertTrue(largeQ.isMatchFor(largeQ));
        assertTrue(xlargeQ.isMatchFor(xlargeQ));

        // test that small screen sizes match the larger ones.
        assertTrue(smallQ.isMatchFor(smallQ));
        assertTrue(smallQ.isMatchFor(normalQ));
        assertTrue(smallQ.isMatchFor(largeQ));
        assertTrue(smallQ.isMatchFor(xlargeQ));
        assertTrue(normalQ.isMatchFor(normalQ));
        assertTrue(normalQ.isMatchFor(largeQ));
        assertTrue(normalQ.isMatchFor(xlargeQ));
        assertTrue(largeQ.isMatchFor(largeQ));
        assertTrue(largeQ.isMatchFor(xlargeQ));
        assertTrue(xlargeQ.isMatchFor(xlargeQ));

        // test that larger screen sizes don't match the smaller ones.
        assertFalse(normalQ.isMatchFor(smallQ));
        assertFalse(largeQ.isMatchFor(smallQ));
        assertFalse(largeQ.isMatchFor(normalQ));
        assertFalse(xlargeQ.isMatchFor(smallQ));
        assertFalse(xlargeQ.isMatchFor(normalQ));
        assertFalse(xlargeQ.isMatchFor(largeQ));
    }

    public void testIsBetterMatchThan() {
        // create qualifiers for small, normal, large and xlarge sizes.
        ScreenSizeQualifier smallQ = new ScreenSizeQualifier(ScreenSize.SMALL);
        ScreenSizeQualifier normalQ = new ScreenSizeQualifier(ScreenSize.NORMAL);
        ScreenSizeQualifier largeQ = new ScreenSizeQualifier(ScreenSize.LARGE);
        ScreenSizeQualifier xlargeQ = new ScreenSizeQualifier(ScreenSize.XLARGE);

        // test that each Q is a better match than all other valid Qs when the ref is the same Q.
        assertTrue(normalQ.isBetterMatchThan(smallQ, normalQ));

        assertTrue(largeQ.isBetterMatchThan(smallQ, largeQ));
        assertTrue(largeQ.isBetterMatchThan(normalQ, largeQ));

        assertTrue(xlargeQ.isBetterMatchThan(smallQ, xlargeQ));
        assertTrue(xlargeQ.isBetterMatchThan(normalQ, xlargeQ));
        assertTrue(xlargeQ.isBetterMatchThan(largeQ, xlargeQ));

        // test that higher screen size if preferable if there's no exact match.
        assertTrue(normalQ.isBetterMatchThan(smallQ, largeQ));
        assertFalse(smallQ.isBetterMatchThan(normalQ, largeQ));

        assertTrue(normalQ.isBetterMatchThan(smallQ, xlargeQ));
        assertTrue(largeQ.isBetterMatchThan(smallQ, xlargeQ));
        assertTrue(largeQ.isBetterMatchThan(normalQ, xlargeQ));

        assertFalse(smallQ.isBetterMatchThan(normalQ, xlargeQ));
        assertFalse(smallQ.isBetterMatchThan(largeQ, xlargeQ));
        assertFalse(normalQ.isBetterMatchThan(largeQ, xlargeQ));
    }

}
