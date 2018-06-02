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

package com.android.ide.common.xml;

import com.android.ide.common.xml.ManifestData.SupportsScreens;

import java.io.InputStream;

import junit.framework.TestCase;

public class SupportsScreensTest extends TestCase {

    private static final String TESTDATA_PATH =
        "/com/android/sdklib/testdata/";  //$NON-NLS-1$
    private static final String TESTAPP2_XML = TESTDATA_PATH +
        "AndroidManifest-testapp2.xml";  //$NON-NLS-1$

    public void testDefaultValuesApi3() {
        SupportsScreens supportsScreens = SupportsScreens.getDefaultValues(3);

        assertNotNull(supportsScreens);
        assertEquals(Boolean.FALSE, supportsScreens.getAnyDensity());
        assertEquals(Boolean.FALSE, supportsScreens.getResizeable());
        assertEquals(Boolean.FALSE, supportsScreens.getSmallScreens());
        assertEquals(Boolean.TRUE, supportsScreens.getNormalScreens());
        assertEquals(Boolean.FALSE, supportsScreens.getLargeScreens());
    }

    public void testDefaultValuesApi4() {
        SupportsScreens supportsScreens = SupportsScreens.getDefaultValues(4);

        assertNotNull(supportsScreens);
        assertEquals(Boolean.TRUE, supportsScreens.getAnyDensity());
        assertEquals(Boolean.TRUE, supportsScreens.getResizeable());
        assertEquals(Boolean.TRUE, supportsScreens.getSmallScreens());
        assertEquals(Boolean.TRUE, supportsScreens.getNormalScreens());
        assertEquals(Boolean.TRUE, supportsScreens.getLargeScreens());
    }

    public void testManifestParsing() throws Exception {
        InputStream manifestStream = this.getClass().getResourceAsStream(TESTAPP2_XML);

        ManifestData data = AndroidManifestParser.parse(manifestStream);
        assertNotNull(data);

        SupportsScreens supportsScreens = data.getSupportsScreensFromManifest();
        assertNotNull(supportsScreens);
        assertEquals(null, supportsScreens.getAnyDensity());
        assertEquals(null, supportsScreens.getResizeable());
        assertEquals(null, supportsScreens.getSmallScreens());
        assertEquals(null, supportsScreens.getNormalScreens());
        assertEquals(Boolean.FALSE, supportsScreens.getLargeScreens());

        supportsScreens = data.getSupportsScreensValues();
        assertNotNull(supportsScreens);
        assertEquals(Boolean.TRUE, supportsScreens.getAnyDensity());
        assertEquals(Boolean.TRUE, supportsScreens.getResizeable());
        assertEquals(Boolean.TRUE, supportsScreens.getSmallScreens());
        assertEquals(Boolean.TRUE, supportsScreens.getNormalScreens());
        assertEquals(Boolean.FALSE, supportsScreens.getLargeScreens());
    }

    public void testOverlapWith() {
        SupportsScreens supportsN = new SupportsScreens("false|false|false|true|false");
        SupportsScreens supportsSL = new SupportsScreens("false|false|true|false|true");

        assertTrue(supportsN.overlapWith(supportsSL));
        assertTrue(supportsSL.overlapWith(supportsN));
    }

    public void testCompareScreenSizesWith() {
        // set instance that support all combo of the three sizes.
        SupportsScreens supportsS = new SupportsScreens("false|false|true|false|false");
        SupportsScreens supportsN = new SupportsScreens("false|false|false|true|false");
        SupportsScreens supportsL = new SupportsScreens("false|false|false|false|true");
        SupportsScreens supportsSL = new SupportsScreens("false|false|true|false|true");

        assertEquals(-1, supportsS.compareScreenSizesWith(supportsN));
        assertEquals( 1, supportsN.compareScreenSizesWith(supportsS));
        assertEquals(-1, supportsN.compareScreenSizesWith(supportsL));
        assertEquals(-1, supportsS.compareScreenSizesWith(supportsL));

        // test thrown exception for overlapWith == true
        try {
            supportsSL.compareScreenSizesWith(supportsN);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // success!
        }

        // test thrown exception for hasStrictlyDifferentScreenSupportAs == false
        try {
            supportsSL.compareScreenSizesWith(supportsS);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // success!
        }

    }

    public void testHasStrictlyDifferentScreenSupportAs() {
        SupportsScreens supportsS = new SupportsScreens("false|false|true|false|false");
        SupportsScreens supportsN = new SupportsScreens("false|false|false|true|false");
        SupportsScreens supportsL = new SupportsScreens("false|false|false|false|true");

        SupportsScreens supportsSN = new SupportsScreens("false|false|true|true|false");
        SupportsScreens supportsNL = new SupportsScreens("false|false|false|true|true");
        SupportsScreens supportsSL = new SupportsScreens("false|false|true|false|true");
        SupportsScreens supportsSNL = new SupportsScreens("false|false|true|true|true");

        assertTrue(supportsS.hasStrictlyDifferentScreenSupportAs(supportsN));
        assertTrue(supportsS.hasStrictlyDifferentScreenSupportAs(supportsL));
        assertTrue(supportsN.hasStrictlyDifferentScreenSupportAs(supportsS));
        assertTrue(supportsN.hasStrictlyDifferentScreenSupportAs(supportsL));
        assertTrue(supportsL.hasStrictlyDifferentScreenSupportAs(supportsS));
        assertTrue(supportsL.hasStrictlyDifferentScreenSupportAs(supportsN));


        assertFalse(supportsSN.hasStrictlyDifferentScreenSupportAs(supportsS));
        assertFalse(supportsSN.hasStrictlyDifferentScreenSupportAs(supportsN));
        assertTrue(supportsSN.hasStrictlyDifferentScreenSupportAs(supportsL));
        assertFalse(supportsSL.hasStrictlyDifferentScreenSupportAs(supportsS));
        assertTrue(supportsSL.hasStrictlyDifferentScreenSupportAs(supportsN));
        assertFalse(supportsSL.hasStrictlyDifferentScreenSupportAs(supportsL));
        assertTrue(supportsNL.hasStrictlyDifferentScreenSupportAs(supportsS));
        assertFalse(supportsNL.hasStrictlyDifferentScreenSupportAs(supportsN));
        assertFalse(supportsNL.hasStrictlyDifferentScreenSupportAs(supportsL));
        assertFalse(supportsSNL.hasStrictlyDifferentScreenSupportAs(supportsS));
        assertFalse(supportsSNL.hasStrictlyDifferentScreenSupportAs(supportsN));
        assertFalse(supportsSNL.hasStrictlyDifferentScreenSupportAs(supportsL));
    }
}
