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

import static com.android.sdklib.SdkVersionInfo.HIGHEST_KNOWN_API;
import static com.android.sdklib.SdkVersionInfo.camelCaseToUnderlines;
import static com.android.sdklib.SdkVersionInfo.getApiByBuildCode;
import static com.android.sdklib.SdkVersionInfo.getApiByPreviewName;
import static com.android.sdklib.SdkVersionInfo.getBuildCode;
import static com.android.sdklib.SdkVersionInfo.getCodeName;
import static com.android.sdklib.SdkVersionInfo.getVersion;
import static com.android.sdklib.SdkVersionInfo.underlinesToCamelCase;

import junit.framework.TestCase;

public class SdkVersionInfoTest extends TestCase {

    public void testGetAndroidName() {
        assertEquals("API 16: Android 4.1 (Jelly Bean)", SdkVersionInfo.getAndroidName(16));
        // Future: if we don't have a name, don't include "null" as a name
        assertEquals("API 500", SdkVersionInfo.getAndroidName(500));
    }

    public void testGetBuildCode() {
        assertEquals("JELLY_BEAN", getBuildCode(16));
    }

    public void testGetApiByPreviewName() {
        assertEquals(5, getApiByPreviewName("Eclair", false));
        assertEquals(18, getApiByPreviewName("JellyBeanMR2", false));
        assertEquals(21, getApiByPreviewName("Lollipop", false));
        assertEquals(21, getApiByPreviewName("L", false));
        assertEquals(-1, getApiByPreviewName("UnknownName", false));
        assertEquals(HIGHEST_KNOWN_API + 1, getApiByPreviewName("UnknownName", true));
    }

    public void testGetApiByBuildCode() {
        assertEquals(7, getApiByBuildCode("ECLAIR_MR1", false));
        assertEquals(16, getApiByBuildCode("JELLY_BEAN", false));

        for (int api = 1; api <= HIGHEST_KNOWN_API; api++) {
            assertEquals(api, getApiByBuildCode(getBuildCode(api), false));
        }

        assertEquals(-1, getApiByBuildCode("K_SURPRISE_SURPRISE", false));
        assertEquals(HIGHEST_KNOWN_API + 1, getApiByBuildCode("K_SURPRISE_SURPRISE", true));
    }

    public void testGetCodeName() {
        assertNull(getCodeName(1));
        assertNull(getCodeName(2));
        assertEquals("Cupcake", getCodeName(3));
        assertEquals("KitKat", getCodeName(19));
        assertEquals("Lollipop", getCodeName(21));
    }

    public void testCamelCaseToUnderlines() {
        assertEquals("", camelCaseToUnderlines(""));
        assertEquals("foo", camelCaseToUnderlines("foo"));
        assertEquals("foo", camelCaseToUnderlines("Foo"));
        assertEquals("foo_bar", camelCaseToUnderlines("FooBar"));
        assertEquals("test_xml", camelCaseToUnderlines("testXML"));
        assertEquals("test_foo", camelCaseToUnderlines("testFoo"));
        assertEquals("jelly_bean_mr2", camelCaseToUnderlines("JellyBeanMR2"));
    }

    public void testUnderlinesToCamelCase() {
        assertEquals("", underlinesToCamelCase(""));
        assertEquals("", underlinesToCamelCase("_"));
        assertEquals("Foo", underlinesToCamelCase("foo"));
        assertEquals("FooBar", underlinesToCamelCase("foo_bar"));
        assertEquals("FooBar", underlinesToCamelCase("foo__bar"));
        assertEquals("Foo", underlinesToCamelCase("foo_"));
        assertEquals("JellyBeanMr2", underlinesToCamelCase("jelly_bean_mr2"));
    }

    @SuppressWarnings("ConstantConditions")
    public void testGetAndroidVersion() {
        assertNull(getVersion("", null));
        assertNull(getVersion("4H", null));
        assertEquals(4, getVersion("4", null).getApiLevel());
        assertNull(getVersion("4", null).getCodename());
        assertEquals("4", getVersion("4", null).getApiString());
        assertEquals(19, getVersion("19", null).getApiLevel());
        // ICS is API 14, but when expressed as a preview platform, it's not yet 14
        assertEquals(13, getVersion("IceCreamSandwich", null).getApiLevel());
        assertEquals("IceCreamSandwich", getVersion("IceCreamSandwich", null).getCodename());
        assertEquals(HIGHEST_KNOWN_API, getVersion("BackToTheFuture", null).getApiLevel());
        assertEquals("BackToTheFuture", getVersion("BackToTheFuture", null).getCodename());
    }
}
