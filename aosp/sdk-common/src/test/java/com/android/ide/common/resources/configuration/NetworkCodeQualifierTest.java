/*
 * Copyright (C) 2008 The Android Open Source Project
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

import junit.framework.TestCase;

public class NetworkCodeQualifierTest extends TestCase {

    private NetworkCodeQualifier mncq;
    private FolderConfiguration config;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mncq = new NetworkCodeQualifier();
        config = new FolderConfiguration();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mncq = null;
        config = null;
    }

    public void testCheckAndSet() {
        assertEquals(true, mncq.checkAndSet("mnc123", config));//$NON-NLS-1$
        assertTrue(config.getNetworkCodeQualifier() != null);
        assertEquals(123, config.getNetworkCodeQualifier().getCode());
        assertEquals("mnc123", config.getNetworkCodeQualifier().toString()); //$NON-NLS-1$
    }

    public void testPrint() {
        assertEquals("mnc123", NetworkCodeQualifier.getFolderSegment(123));
        assertEquals("mnc012", NetworkCodeQualifier.getFolderSegment(12));
        assertEquals("mnc001", NetworkCodeQualifier.getFolderSegment(1));
        assertEquals("", NetworkCodeQualifier.getFolderSegment(0));
    }

    public void testFailures() {
        assertEquals(false, mncq.checkAndSet("", config));//$NON-NLS-1$
        assertEquals(false, mncq.checkAndSet("mnc", config));//$NON-NLS-1$
        assertEquals(false, mncq.checkAndSet("MNC123", config));//$NON-NLS-1$
        assertEquals(false, mncq.checkAndSet("123", config));//$NON-NLS-1$
        assertEquals(false, mncq.checkAndSet("mncsdf", config));//$NON-NLS-1$
    }

}
