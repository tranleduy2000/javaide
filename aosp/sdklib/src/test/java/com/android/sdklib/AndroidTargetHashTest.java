/*
 * Copyright (C) 2013 The Android Open Source Project
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

import com.android.sdklib.internal.androidTarget.MockAddonTarget;
import com.android.sdklib.internal.androidTarget.MockPlatformTarget;

import junit.framework.TestCase;

public class AndroidTargetHashTest extends TestCase {

    public final void testGetPlatformHashString() {
        assertEquals("android-10",
                AndroidTargetHash.getPlatformHashString(new AndroidVersion(10, null)));

        assertEquals("android-CODE_NAME",
                AndroidTargetHash.getPlatformHashString(new AndroidVersion(10, "CODE_NAME")));
    }

    public final void testGetAddonHashString() {
        assertEquals("The Vendor Inc.:My Addon:10",
                AndroidTargetHash.getAddonHashString(
                        "The Vendor Inc.",
                        "My Addon",
                        new AndroidVersion(10, null)));
    }

    public final void testGetTargetHashString() {
        MockPlatformTarget t = new MockPlatformTarget(10, 1);
        assertEquals("android-10", AndroidTargetHash.getTargetHashString(t));
        MockAddonTarget a = new MockAddonTarget("My Addon", t, 2);
        assertEquals("vendor 10:My Addon:10", AndroidTargetHash.getTargetHashString(a));
    }

    public void testGetPlatformVersion() {
        assertNull(AndroidTargetHash.getPlatformVersion("blah-5"));
        assertNull(AndroidTargetHash.getPlatformVersion("5-blah"));
        assertNull(AndroidTargetHash.getPlatformVersion("android-"));

        AndroidVersion version = AndroidTargetHash.getPlatformVersion("android-5");
        assertNotNull(version);
        assertEquals(5, version.getApiLevel());
        assertNull(version.getCodename());

        version = AndroidTargetHash.getPlatformVersion("5");
        assertNotNull(version);
        assertEquals(5, version.getApiLevel());
        assertNull(version.getCodename());

        version = AndroidTargetHash.getPlatformVersion("android-CUPCAKE");
        assertNotNull(version);
        assertEquals(3, version.getApiLevel());
        assertEquals("CUPCAKE", version.getCodename());

        version = AndroidTargetHash.getPlatformVersion("android-KITKAT");
        assertNotNull(version);
        assertEquals(19, version.getApiLevel());
        assertEquals("KITKAT", version.getCodename());

        version = AndroidTargetHash.getPlatformVersion("android-UNKNOWN");
        assertNotNull(version);
        assertEquals(1, version.getApiLevel());
        assertEquals("UNKNOWN", version.getCodename());
    }
}
