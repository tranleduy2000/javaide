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

package com.android.sdklib.internal.repository.packages;

import com.android.sdklib.repository.FullRevision;
import com.android.sdklib.repository.descriptors.PkgDesc;

import junit.framework.TestCase;

public class BrokenPackageTest extends TestCase {

    private BrokenPackage m;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        m = new BrokenPackage(null /*props*/,
                "short description",
                "long description",
                12, // min api level
                13, // exact api level
                "os/path",
                PkgDesc.Builder.newTool(new FullRevision(1, 2, 3, 4),
                                        FullRevision.NOT_SPECIFIED).create());
    }

    public final void testGetShortDescription() {
        assertEquals("short description", m.getShortDescription());
    }

    public final void testGetLongDescription() {
        assertEquals("long description", m.getLongDescription());
    }

    public final void testGetMinApiLevel() {
        assertEquals(12, m.getMinApiLevel());
    }

    public final void testGetExactApiLevel() {
        assertEquals(13, m.getExactApiLevel());
    }

    public void testInstallId() {
        assertEquals("", m.installId());
    }

    public final void testGetPkgDesc() {
        assertEquals(
                PkgDesc.Builder.newTool(new FullRevision(1, 2, 3, 4),
                                        FullRevision.NOT_SPECIFIED).create(),
                m.getPkgDesc());
    }
}
