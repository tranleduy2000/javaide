/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Eclipse Public License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/org/documents/epl-v10.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.sdklib;

import com.android.sdklib.SdkManager.LayoutlibVersion;

import junit.framework.TestCase;

/**
 * Unit test for {@link LayoutlibVersion}.
 */
public class LayoutlibVersionTest extends TestCase {

    public void testLayoutlibVersion_create() {
        LayoutlibVersion lv = new LayoutlibVersion(1, 2);
        assertEquals(1, lv.getApi());
        assertEquals(2, lv.getRevision());
    }

    public void testLayoutlibVersion_compare() {
        assertTrue(new LayoutlibVersion(1, 1).compareTo(new LayoutlibVersion(1, 1)) == 0);
        assertTrue(new LayoutlibVersion(1, 2).compareTo(new LayoutlibVersion(1, 1)) >  0);
        assertTrue(new LayoutlibVersion(1, 1).compareTo(new LayoutlibVersion(1, 2)) <  0);
        assertTrue(new LayoutlibVersion(2, 2).compareTo(new LayoutlibVersion(1, 3)) >  0);

        // the lack of an API (== 0) naturally sorts as the lowest value possible.
        assertTrue(new LayoutlibVersion(0, 1).compareTo(new LayoutlibVersion(0, 2)) <  0);
        assertTrue(new LayoutlibVersion(0, 1).compareTo(new LayoutlibVersion(1, 2)) <  0);
        assertTrue(new LayoutlibVersion(0, 3).compareTo(new LayoutlibVersion(1, 2)) <  0);
        assertTrue(new LayoutlibVersion(1, 2).compareTo(new LayoutlibVersion(0, 3)) >  0);

        // if we lack the revision number, we don't use it in comparison
        assertTrue(new LayoutlibVersion(2, 0).compareTo(new LayoutlibVersion(2, 2)) == 0);
        assertTrue(new LayoutlibVersion(2, 2).compareTo(new LayoutlibVersion(2, 0)) == 0);
    }

}
