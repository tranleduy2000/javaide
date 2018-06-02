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

package com.android.sdklib.internal.repository.packages;

import com.android.sdklib.repository.PkgProps;

import java.util.Properties;

public class ExtraPackageTest_Base extends PackageTest {

    @Override
    public void testCreate() {
        Properties props = createExpectedProps();

        MockExtraPackage p = new MockExtraPackage(
                null, //source
                props,
                "vendor",
                "the_path",
                -1, //revision
                null, //license
                null, //description
                null, //descUrl
                LOCAL_ARCHIVE_PATH
                );

        testCreatedPackage(p);
    }

    @Override
    public void testSaveProperties() {
        Properties expected = createExpectedProps();

        MockExtraPackage p = new MockExtraPackage(
                null, //source
                expected,
                "vendor",
                "the_path",
                -1, //revision
                null, //license
                null, //description
                null, //descUrl
                LOCAL_ARCHIVE_PATH
                );

        Properties actual = new Properties();
        p.saveProperties(actual);

        assertEquals(expected, actual);
    }

    @Override
    protected Properties createExpectedProps() {
        Properties props = super.createExpectedProps();

        props.setProperty(PkgProps.EXTRA_VENDOR_ID,      "vendor");
        props.setProperty(PkgProps.EXTRA_VENDOR_DISPLAY, "vendor");
        props.setProperty(PkgProps.EXTRA_PATH,           "the_path");
        props.setProperty(PkgProps.EXTRA_NAME_DISPLAY,   "Vendor The Path");

        // Extra revision is now a NoPreviewRevision and writes its full major.minor.micro
        props.setProperty(PkgProps.PKG_REVISION, "42.0.0");

        // MinToolsPackage properties
        props.setProperty(PkgProps.MIN_TOOLS_REV, "3.0.1");

        return props;
    }

    protected void testCreatedMinToolsPackage(MockExtraPackage p) {
        super.testCreatedPackage(p);

        // MinToolsPackage properties
        assertEquals("3.0.1", p.getMinToolsRevision().toShortString());
    }
}
