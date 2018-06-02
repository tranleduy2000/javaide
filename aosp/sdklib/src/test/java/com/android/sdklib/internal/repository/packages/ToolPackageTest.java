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

import com.android.sdklib.internal.repository.archives.Archive;
import com.android.sdklib.repository.FullRevision;
import com.android.sdklib.repository.PkgProps;

import java.util.Properties;

import junit.framework.TestCase;

public class ToolPackageTest extends TestCase {

    private static class ToolPackageFakeArchive extends ToolPackage {
        protected ToolPackageFakeArchive(Properties props) {
            super(null, // source
                    props,
                    0,    //ignored, the one from props will be used
                    null, //license
                    null, //description
                    null, //descUrl
                    "/sdk/tools");
        }

        @Override
        protected Archive[] initializeArchives(
                Properties props,
                String archiveOsPath) {
            return super.initializeArchives(props, PackageTest.LOCAL_ARCHIVE_PATH);
        }
    }

    private Properties createExpectedProps(boolean isPreview) {
        Properties props = PackageTest.createDefaultProps();

        props.setProperty(PkgProps.PKG_REVISION,
                new FullRevision(1, 2, 3, isPreview ? 4 : 0).toShortString());
        props.setProperty(PkgProps.VERSION_API_LEVEL, "5");

        return props;
    }

    // ----

    public void testInstallId() throws Exception {
        Properties props1 = createExpectedProps(true);
        ToolPackage p1 = new ToolPackageFakeArchive(props1);
        assertEquals("tools-preview", p1.installId());

        Properties props2 = createExpectedProps(false);
        ToolPackage p2 = new ToolPackageFakeArchive(props2);
        assertEquals("tools", p2.installId());
    }
}
