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

import com.android.sdklib.SdkManager;
import com.android.sdklib.internal.repository.sources.SdkSource;
import com.android.sdklib.repository.FullRevision;
import com.android.sdklib.repository.PkgProps;
import com.android.sdklib.repository.descriptors.IPkgDesc;
import com.android.sdklib.repository.descriptors.PkgDesc;

import java.io.File;
import java.util.Properties;

public class MinToolsPackageTest extends PackageTest {

    /** Local class used to test the abstract MinToolsPackage class */
    protected static class MockMinToolsPackage extends MinToolsPackage {
        public MockMinToolsPackage(
                SdkSource source,
                Properties props,
                int revision,
                String license,
                String description,
                String descUrl,
                String archiveOsPath) {
            super(source,
                    props,
                    revision,
                    license,
                    description,
                    descUrl,
                    archiveOsPath);
        }

        @Override
        public File getInstallFolder(String osSdkRoot, SdkManager sdkManager) {
            throw new UnsupportedOperationException("abstract method not used in test"); //$NON-NLS-1$
        }

        @Override
        public String getListDescription() {
            throw new UnsupportedOperationException("abstract method not used in test"); //$NON-NLS-1$
        }

        @Override
        public String getShortDescription() {
            throw new UnsupportedOperationException("abstract method not used in test"); //$NON-NLS-1$
        }

        @Override
        public boolean sameItemAs(Package pkg) {
            throw new UnsupportedOperationException("abstract method not used in test"); //$NON-NLS-1$
        }

        @Override
        public String installId() {
            return "";  //$NON-NLS-1$
        }

        @Override
        public IPkgDesc getPkgDesc() {
            return PkgDesc.Builder.newTool(
                    new FullRevision(1, 2, 3, 4),
                    FullRevision.NOT_SPECIFIED).create();
        }
    }

    @Override
    public void testCreate() {
        Properties props = createExpectedProps();

        MockMinToolsPackage p = new MockMinToolsPackage(
                null, //source
                props,
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

        MockMinToolsPackage p = new MockMinToolsPackage(
                null, //source
                expected,
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

        // MinToolsPackage properties
        props.setProperty(PkgProps.MIN_TOOLS_REV, "3.0.1");

        return props;
    }

    protected void testCreatedMinToolsPackage(MockMinToolsPackage p) {
        super.testCreatedPackage(p);

        // MinToolsPackage properties
        assertEquals("3.0.1", p.getMinToolsRevision().toShortString());
    }
}
