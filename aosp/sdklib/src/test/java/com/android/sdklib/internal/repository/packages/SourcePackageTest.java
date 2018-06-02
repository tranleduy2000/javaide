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

import com.android.sdklib.AndroidVersion;
import com.android.sdklib.AndroidVersion.AndroidVersionException;
import com.android.sdklib.internal.repository.archives.Archive;
import com.android.sdklib.repository.PkgProps;

import java.util.Properties;

public class SourcePackageTest extends PackageTest {

    private static class SourcePackageFakeArchive extends SourcePackage {
        protected SourcePackageFakeArchive(
                AndroidVersion platformVersion,
                int revision,
                Properties props) {
            super(platformVersion,
                    revision,
                    props,
                    String.format("/sdk/sources/android-%s", platformVersion.getApiString()));
        }

        @Override
        protected Archive[] initializeArchives(
                Properties props,
                String archiveOsPath) {
            return super.initializeArchives(props, LOCAL_ARCHIVE_PATH);
        }
    }

    private SourcePackage createSourcePackageTest(Properties props) throws AndroidVersionException {
        SourcePackage p = new SourcePackageFakeArchive(
                new AndroidVersion(props),
                1 /*revision*/,
                props);
        return p;
    }

    @Override
    protected Properties createExpectedProps() {
        Properties props = super.createExpectedProps();

        // SourcePackageTest properties
        props.setProperty(PkgProps.VERSION_API_LEVEL, "5");

        return props;
    }

    protected void testCreatedSourcePackageTest(SourcePackage p) {
        super.testCreatedPackage(p);

        // SourcePackageTest properties
        assertEquals("API 5", p.getAndroidVersion().toString());
    }

    // ----

    @Override
    public final void testCreate() throws Exception {
        Properties props = createExpectedProps();
        SourcePackage p = createSourcePackageTest(props);

        testCreatedSourcePackageTest(p);
    }

    @Override
    public void testSaveProperties() throws Exception {
        Properties expected = createExpectedProps();
        SourcePackage p = createSourcePackageTest(expected);

        Properties actual = new Properties();
        p.saveProperties(actual);

        assertEquals(expected, actual);
    }

    public void testSameItemAs() throws Exception {
        Properties props1 = createExpectedProps();
        SourcePackage p1 = createSourcePackageTest(props1);
        assertTrue(p1.sameItemAs(p1));

        // different version
        Properties props2 = new Properties(props1);
        props2.setProperty(PkgProps.VERSION_API_LEVEL, "6");
        SourcePackage p2 = createSourcePackageTest(props2);
        assertFalse(p1.sameItemAs(p2));
        assertFalse(p2.sameItemAs(p1));
    }

    public void testInstallId() throws Exception {
        Properties props = createExpectedProps();
        SourcePackage p = createSourcePackageTest(props);

        assertEquals("source-5", p.installId());
    }
}
