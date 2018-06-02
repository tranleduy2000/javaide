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

import com.android.sdklib.repository.PkgProps;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;

/**
 * Tests {@link ExtraPackage} using anddon-3.xsd: it has a {@code <path>} and {@code <vendor>}.
 * (it lacks name-display, vendor-id and vendor-display which are in addon-4.xsd)
 */
public class ExtraPackageTest_v3 extends ExtraPackageTest_Base {

    private static final char PS = File.pathSeparatorChar;

    private ExtraPackage createExtraPackage(Properties props) {
        ExtraPackage p = (ExtraPackage) ExtraPackage.create(
                null, //source
                props,
                null, //vendor
                null, //path
                -1, //revision
                null, //license
                null, //description
                null, //descUrl
                "/local/archive/path" //archiveOsPath
                );
        return p;
    }

    /** Properties used to "load" the package. When saved, they become different. */
    private Properties createLoadedProps() {
        Properties props = super.createExpectedProps();

        // ExtraPackage properties
        props.setProperty(PkgProps.EXTRA_VENDOR, "vendor");
        props.setProperty(PkgProps.EXTRA_PATH, "the_path");
        props.setProperty(PkgProps.EXTRA_OLD_PATHS, "old_path1;oldpath2");
        props.setProperty(PkgProps.EXTRA_MIN_API_LEVEL, "11");
        props.setProperty(PkgProps.EXTRA_PROJECT_FILES,
                "path1.jar" + PS + "dir2/jar 2.jar" + PS + "dir/3/path");

        return props;
    }

    /** Properties saved by the package. They differ from loaded ones in name and vendor. */
    private Properties createSavedProps() {
        Properties props = super.createExpectedProps();

        // ExtraPackage properties
        props.setProperty(PkgProps.EXTRA_VENDOR_ID, "vendor");
        props.setProperty(PkgProps.EXTRA_VENDOR_DISPLAY, "vendor");
        props.setProperty(PkgProps.EXTRA_PATH, "the_path");
        props.setProperty(PkgProps.EXTRA_NAME_DISPLAY, "Vendor The Path");
        props.setProperty(PkgProps.EXTRA_OLD_PATHS, "old_path1;oldpath2");
        props.setProperty(PkgProps.EXTRA_MIN_API_LEVEL, "11");
        props.setProperty(PkgProps.EXTRA_PROJECT_FILES,
                "path1.jar" + PS + "dir2/jar 2.jar" + PS + "dir/3/path");

        return props;
    }

    protected void testCreatedExtraPackage(ExtraPackage p) {
        super.testCreatedPackage(p);

        // Package properties
        // vendor becomes both vendor-id and vendor-display
        assertEquals("vendor", p.getVendorId());
        assertEquals("vendor", p.getVendorDisplay());
        assertEquals("the_path", p.getPath());
        // path and vendor are combined in the default display name
        assertEquals("Vendor The Path", p.getDisplayName());
        assertEquals("[old_path1, oldpath2]", Arrays.toString(p.getOldPaths()));
        assertEquals(11, p.getMinApiLevel());
        assertEquals(
                "[path1.jar, dir2/jar 2.jar, dir/3/path]",
                Arrays.toString(p.getProjectFiles()));
    }

    // ----

    @Override
    public final void testCreate() {
        Properties props = createLoadedProps();
        ExtraPackage p = createExtraPackage(props);

        testCreatedExtraPackage(p);
    }

    @Override
    public void testSaveProperties() {
        Properties props = createLoadedProps();
        ExtraPackage p = createExtraPackage(props);

        Properties props2 = new Properties();
        p.saveProperties(props2);

        assertEquals(props2, createSavedProps());
    }

    public void testSameItemAs() {
        Properties props1 = createLoadedProps();
        ExtraPackage p1 = createExtraPackage(props1);
        assertTrue(p1.sameItemAs(p1));

        // different vendor, same path
        Properties props2 = new Properties(props1);
        props2.setProperty(PkgProps.EXTRA_VENDOR_ID, "");
        props2.setProperty(PkgProps.EXTRA_VENDOR_DISPLAY, "");
        props2.setProperty(PkgProps.EXTRA_NAME_DISPLAY, "");
        props2.setProperty(PkgProps.EXTRA_VENDOR, "vendor2");
        ExtraPackage p2 = createExtraPackage(props2);
        assertFalse(p1.sameItemAs(p2));
        assertFalse(p2.sameItemAs(p1));

        // different vendor, different path
        props2.setProperty(PkgProps.EXTRA_PATH, "new_path2");
        p2 = createExtraPackage(props2);
        assertFalse(p1.sameItemAs(p2));
        assertFalse(p2.sameItemAs(p1));

        // same vendor, but single path using the old paths from p1
        Properties props3 = new Properties(props1);
        props3.setProperty(PkgProps.EXTRA_OLD_PATHS, "");
        props3.setProperty(PkgProps.EXTRA_PATH, "old_path1");
        ExtraPackage p3 = createExtraPackage(props3);
        assertTrue(p1.sameItemAs(p3));
        assertTrue(p3.sameItemAs(p1));

        props3.setProperty(PkgProps.EXTRA_PATH, "oldpath2");
        p3 = createExtraPackage(props3);
        assertTrue(p1.sameItemAs(p3));
        assertTrue(p3.sameItemAs(p1));

        // same vendor, different old paths but there's a path=>old_path match
        Properties props4 = new Properties(props1);
        props4.setProperty(PkgProps.EXTRA_OLD_PATHS, "new_path4;new_path5");
        props4.setProperty(PkgProps.EXTRA_PATH, "old_path1");
        ExtraPackage p4 = createExtraPackage(props4);
        assertTrue(p1.sameItemAs(p4));
        assertTrue(p4.sameItemAs(p1));

        // same vendor, incompatible paths
        Properties props5 = new Properties(props1);
        // and the only match is between old_paths, which doesn't count.
        props5.setProperty(PkgProps.EXTRA_OLD_PATHS, "old_path1;new_path5");
        props5.setProperty(PkgProps.EXTRA_PATH, "new_path4");
        ExtraPackage p5 = createExtraPackage(props5);
        assertFalse(p1.sameItemAs(p5));
        assertFalse(p5.sameItemAs(p1));
    }

    public void testInstallId() {
        Properties props = createLoadedProps();
        ExtraPackage p = createExtraPackage(props);

        assertEquals("extra-vendor-the_path", p.installId());
    }
}
