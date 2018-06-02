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

package com.android.sdklib;

import com.android.sdklib.AndroidVersion.AndroidVersionException;

import junit.framework.TestCase;

/**
 * Unit tests for {@link AndroidVersion}.
 */
public class AndroidVersionTest extends TestCase {

    public final void testAndroidVersion() {
        AndroidVersion v = new AndroidVersion(1, "  CODENAME   ");
        assertEquals(1, v.getApiLevel());
        assertEquals("CODENAME", v.getApiString());
        assertTrue(v.isPreview());
        assertEquals("CODENAME", v.getCodename());
        assertEquals("CODENAME".hashCode(), v.hashCode());
        assertEquals("API 1, CODENAME preview", v.toString());

        v = new AndroidVersion(15, "REL");
        assertEquals(15, v.getApiLevel());
        assertEquals("15", v.getApiString());
        assertFalse(v.isPreview());
        assertNull(v.getCodename());
        assertTrue(v.equals(15));
        assertEquals(15, v.hashCode());
        assertEquals("API 15", v.toString());

        v = new AndroidVersion(15, null);
        assertEquals(15, v.getApiLevel());
        assertEquals("15", v.getApiString());
        assertFalse(v.isPreview());
        assertNull(v.getCodename());
        assertTrue(v.equals(15));
        assertEquals(15, v.hashCode());
        assertEquals("API 15", v.toString());

        // An empty codename is like a null codename
        v = new AndroidVersion(15, "   ");
        assertFalse(v.isPreview());
        assertNull(v.getCodename());
        assertEquals("15", v.getApiString());

        v = new AndroidVersion(15, "");
        assertFalse(v.isPreview());
        assertNull(v.getCodename());
        assertEquals("15", v.getApiString());

        assertTrue(v.isGreaterOrEqualThan(0));
        assertTrue(v.isGreaterOrEqualThan(14));
        assertTrue(v.isGreaterOrEqualThan(15));
        assertFalse(v.isGreaterOrEqualThan(16));
        assertFalse(v.isGreaterOrEqualThan(Integer.MAX_VALUE));
   }

    public final void testAndroidVersion_apiOrCodename() throws AndroidVersionException {
        // A valid integer is considered an API level
        AndroidVersion v = new AndroidVersion("15");
        assertEquals(15, v.getApiLevel());
        assertEquals("15", v.getApiString());
        assertFalse(v.isPreview());
        assertNull(v.getCodename());
        assertTrue(v.equals(15));
        assertEquals(15, v.hashCode());
        assertEquals("API 15", v.toString());

        // A valid name is considered a codename
        v = new AndroidVersion("CODE_NAME");
        assertEquals("CODE_NAME", v.getApiString());
        assertTrue(v.isPreview());
        assertEquals("CODE_NAME", v.getCodename());
        assertTrue(v.equals("CODE_NAME"));
        assertEquals(0, v.getApiLevel());
        assertEquals("API 0, CODE_NAME preview", v.toString());

        // invalid code name should fail
        for (String s : new String[] { "REL", "code.name", "10val", "" }) {
            try {
                v = new AndroidVersion(s);
                fail("Invalid code name '" + s + "': Expected to fail. Actual: did not fail.");
            } catch (AndroidVersionException e) {
                assertEquals("Invalid android API or codename " + s, e.getMessage());
            }
        }
    }

    public void testGetFeatureLevel() {
        assertEquals(1, AndroidVersion.DEFAULT.getFeatureLevel());

        assertEquals(5, new AndroidVersion(5, null).getApiLevel());
        assertEquals(5, new AndroidVersion(5, null).getFeatureLevel());

        assertEquals(5, new AndroidVersion(5, "codename").getApiLevel());
        assertEquals(6, new AndroidVersion(5, "codename").getFeatureLevel());
    }
}
