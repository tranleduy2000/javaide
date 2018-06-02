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

package com.android.sdklib.internal.repository.packages;

import com.android.sdklib.AndroidVersion;
import com.android.sdklib.AndroidVersion.AndroidVersionException;
import com.android.sdklib.internal.repository.archives.Archive;
import com.android.sdklib.repository.PkgProps;

import java.util.Properties;

public class AddonSystemImagePackageTest extends PackageTest {

    private static class SysImgPackageFakeArchive extends SystemImagePackage {
        protected SysImgPackageFakeArchive(
                AndroidVersion platformVersion,
                int revision,
                String addonVendorId,
                String addonNameId,
                String abi,
                Properties props) {
            super(platformVersion,
                    revision,
                    abi,
                    props,
                    String.format("/sdk/system-images/addon-%s-%s-%s/%s",
                            addonNameId,
                            addonVendorId,
                            platformVersion.getApiString(),
                            abi));
        }

        @Override
        protected Archive[] initializeArchives(
                Properties props,
                String archiveOsPath) {
            return super.initializeArchives(props, LOCAL_ARCHIVE_PATH);
        }
    }

    private SystemImagePackage createSystemImagePackage(Properties props)
            throws AndroidVersionException {
        SystemImagePackage p = new SysImgPackageFakeArchive(
                new AndroidVersion(props),
                1 /*revision*/,
                props.getProperty(PkgProps.ADDON_VENDOR_ID),
                props.getProperty(PkgProps.SYS_IMG_TAG_ID),
                props.getProperty(PkgProps.SYS_IMG_ABI),
                props);
        return p;
    }

    @Override
    protected Properties createExpectedProps() {
        Properties props = super.createExpectedProps();

        // SystemImagePackage properties
        props.setProperty(PkgProps.VERSION_API_LEVEL,   "5");
        props.setProperty(PkgProps.SYS_IMG_ABI,         "armeabi-v7a");

        // Addon-specific SystemImagePackage properties
        props.setProperty(PkgProps.ADDON_VENDOR_ID, "vendor_id");
        props.setProperty(PkgProps.ADDON_VENDOR_DISPLAY, "Vendor Name");
        props.setProperty(PkgProps.SYS_IMG_TAG_ID, "addon_name");
        props.setProperty(PkgProps.SYS_IMG_TAG_DISPLAY, "Add-on Name");

        return props;
    }

    protected void testCreatedSystemImagePackage(SystemImagePackage p) {
        super.testCreatedPackage(p);

        // SystemImagePackage properties
        assertEquals("API 5", p.getAndroidVersion().toString());
        assertEquals("armeabi-v7a", p.getAbi());
    }

    // ----

    @Override
    public final void testCreate() throws Exception {
        Properties props = createExpectedProps();
        SystemImagePackage p = createSystemImagePackage(props);

        testCreatedSystemImagePackage(p);
    }

    @Override
    public void testSaveProperties() throws Exception {
        Properties expected = createExpectedProps();
        SystemImagePackage p = createSystemImagePackage(expected);

        Properties actual = new Properties();
        p.saveProperties(actual);

        assertEquals(expected, actual);
    }

    public void testSameItemAs() throws Exception {
        Properties props1 = createExpectedProps();
        SystemImagePackage p1 = createSystemImagePackage(props1);
        assertTrue(p1.sameItemAs(p1));

        // different abi, same version
        Properties props2 = new Properties(props1);
        props2.setProperty(PkgProps.SYS_IMG_ABI, "x86");
        SystemImagePackage p2 = createSystemImagePackage(props2);
        assertFalse(p1.sameItemAs(p2));
        assertFalse(p2.sameItemAs(p1));

        // different abi, different version
        props2.setProperty(PkgProps.VERSION_API_LEVEL, "6");
        p2 = createSystemImagePackage(props2);
        assertFalse(p1.sameItemAs(p2));
        assertFalse(p2.sameItemAs(p1));

        // same abi, different version
        Properties props3 = new Properties(props1);
        props3.setProperty(PkgProps.VERSION_API_LEVEL, "6");
        SystemImagePackage p3 = createSystemImagePackage(props3);
        assertFalse(p1.sameItemAs(p3));
        assertFalse(p3.sameItemAs(p1));
    }

    public void testInstallId() throws Exception {
        Properties props = createExpectedProps();
        SystemImagePackage p = createSystemImagePackage(props);

        assertEquals("sys-img-armeabi-v7a-addon-addon_name-vendor_id-5", p.installId());
    }
}
