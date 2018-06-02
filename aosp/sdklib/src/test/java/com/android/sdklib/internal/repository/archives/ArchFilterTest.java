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

package com.android.sdklib.internal.repository.archives;

import com.android.annotations.NonNull;
import com.android.sdklib.repository.FullRevision;
import com.android.sdklib.repository.NoPreviewRevision;

import junit.framework.TestCase;

public class ArchFilterTest extends TestCase {

    public void testGetCurrent() {
        ArchFilter f1 = makeCurrent("Windows 7", "amd64", "1.7.0_51");
        assertEquals(HostOs.WINDOWS, f1.getHostOS());
        assertEquals(BitSize._64, f1.getHostBits());
        assertEquals(BitSize._64, f1.getJvmBits());
        assertEquals(new FullRevision(1, 7, 0), f1.getMinJvmVersion());

        ArchFilter f2 = makeCurrent("Mac OS X", "x86_64", "1.7.0_51");
        assertEquals(HostOs.MACOSX, f2.getHostOS());
        assertEquals(BitSize._64, f2.getHostBits());
        assertEquals(BitSize._64, f2.getJvmBits());
        assertEquals(new FullRevision(1, 7, 0), f2.getMinJvmVersion());

        ArchFilter f3 = makeCurrent("Linux", "x86", "1.6.42_43");
        assertEquals(HostOs.LINUX, f3.getHostOS());
        assertEquals(BitSize._32, f3.getHostBits());
        assertEquals(BitSize._32, f3.getJvmBits());
        assertEquals(new FullRevision(1, 6, 42), f3.getMinJvmVersion());
    }

    public void testIsCompatibleWith() {
        ArchFilter f1 = makeCurrent("Windows 7", "amd64", "1.7.0_51");

        assertTrue(new ArchFilter(null, null, null, null).isCompatibleWith(f1));

        assertTrue (new ArchFilter(HostOs.WINDOWS, null, null, null).isCompatibleWith(f1));
        assertFalse(new ArchFilter(HostOs.MACOSX , null, null, null).isCompatibleWith(f1));
        assertFalse(new ArchFilter(HostOs.LINUX  , null, null, null).isCompatibleWith(f1));

        assertTrue (new ArchFilter(null, BitSize._64, null, null).isCompatibleWith(f1));
        assertFalse(new ArchFilter(null, BitSize._32, null, null).isCompatibleWith(f1));

        assertTrue (new ArchFilter(null, null, BitSize._64, null).isCompatibleWith(f1));
        assertFalse(new ArchFilter(null, null, BitSize._32, null).isCompatibleWith(f1));

        assertTrue (new ArchFilter(null, null, null, new NoPreviewRevision(1, 6, 42)).isCompatibleWith(f1));
        assertTrue (new ArchFilter(null, null, null, new NoPreviewRevision(1, 7,  0)).isCompatibleWith(f1));
        assertFalse(new ArchFilter(null, null, null, new NoPreviewRevision(1, 7,  1)).isCompatibleWith(f1));
        assertFalse(new ArchFilter(null, null, null, new NoPreviewRevision(1, 8,  0)).isCompatibleWith(f1));
        assertFalse(new ArchFilter(null, null, null, new NoPreviewRevision(2, 0,  0)).isCompatibleWith(f1));
    }

    // ---- helpers ---

    /**
     * {@link ArchFilter#getCurrent()} uses java system properties to find the
     * current architecture attributes. This method temporarily overrides
     * System properties, calls {@link ArchFilter#getCurrent()} and then reset
     * the properties.
     *
     * @param osName The override value for the System "os.name" property.
     * @param osArch The override value for the System "os.arch" property.
     * @param javaVersion The override value for the System "java.version" property.
     * @return A new {@link ArchFilter}
     */
    @NonNull
    private ArchFilter makeCurrent(@NonNull String osName,
                                   @NonNull String osArch,
                                   @NonNull String javaVersion) {
        String oldOsName   = System.getProperty("os.name");
        String oldOsArch   = System.getProperty("os.arch");
        String oldJavaVers = System.getProperty("java.version");
        try {
            System.setProperty("os.name", osName);
            System.setProperty("os.arch", osArch);
            System.setProperty("java.version", javaVersion);

            return ArchFilter.getCurrent();
        } finally {
            System.setProperty("os.name", oldOsName);
            System.setProperty("os.arch", oldOsArch);
            System.setProperty("java.version", oldJavaVers);
        }
    }

}
