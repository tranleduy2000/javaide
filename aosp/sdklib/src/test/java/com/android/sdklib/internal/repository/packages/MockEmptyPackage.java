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
import com.android.sdklib.internal.repository.archives.Archive;
import com.android.sdklib.internal.repository.sources.SdkSource;
import com.android.sdklib.repository.FullRevision;
import com.android.sdklib.repository.descriptors.IPkgDesc;
import com.android.sdklib.repository.descriptors.PkgDesc;

import java.io.File;
import java.util.Properties;

/**
 * A mock empty package, of no particular subpackage type.
 * {@link #sameItemAs(Package)} will return true if these packages have the same handle.
 */
public class MockEmptyPackage extends MajorRevisionPackage {
    private final String mTestHandle;

    /**
     * Creates a new {@link MockEmptyPackage} with a local archive.
     *
     * @param testHandle The comparison handle for {@link #sameItemAs(Package)}.
     */
    public MockEmptyPackage(String testHandle) {
        super(
            null /*source*/,
            null /*props*/,
            0 /*revision*/,
            null /*license*/,
            null /*description*/,
            null /*descUrl*/,
            "/sdk/tmp/empty_pkg" /*archiveOsPath*/
            );
        mTestHandle = testHandle;
    }

    /**
     * Creates a new {@link MockEmptyPackage} with a local archive.
     *
     * @param testHandle The comparison handle for {@link #sameItemAs(Package)}.
     * @param props The package properties.
     */
    public MockEmptyPackage(String testHandle, Properties props) {
        super(
            null /*source*/,
            props,
            0 /*revision*/,
            null /*license*/,
            null /*description*/,
            null /*descUrl*/,
            "/sdk/tmp/empty_pkg" /*archiveOsPath*/
            );
        mTestHandle = testHandle;
    }

    /**
     * Creates a new {@link MockEmptyPackage} with a local archive.
     *
     * @param testHandle The comparison handle for {@link #sameItemAs(Package)}.
     * @param revision The revision of the package, printed in the short description.
     */
    public MockEmptyPackage(String testHandle, int revision) {
        super(
            null /*source*/,
            null /*props*/,
            revision,
            null /*license*/,
            null /*description*/,
            null /*descUrl*/,
            "/sdk/tmp/empty_pkg" /*archiveOsPath*/
            );
        mTestHandle = testHandle;
    }

    /**
     * Creates a new {@link MockEmptyPackage} with a local archive.
     *
     * @param source The source associate with this package.
     * @param testHandle The comparison handle for {@link #sameItemAs(Package)}.
     * @param revision The revision of the package, printed in the short description.
     */
    public MockEmptyPackage(SdkSource source, String testHandle, int revision) {
        super(
            source,
            null /*props*/,
            revision,
            null /*license*/,
            null /*description*/,
            null /*descUrl*/,
            "/sdk/tmp/empty_pkg" /*archiveOsPath*/
            );
        mTestHandle = testHandle;
    }

    @Override
    protected Archive[] initializeArchives(
            Properties props,
            String archiveOsPath) {
        return new Archive[] {
            new Archive(this, props, archiveOsPath) {
                @Override
                public String toString() {
                    return mTestHandle;
                }
            } };
    }

    @Override
    public IPkgDesc getPkgDesc() {
        return PkgDesc.Builder.newTool(
                new FullRevision(1, 2, 3, 4),
                FullRevision.NOT_SPECIFIED).create();
    }

    public Archive getLocalArchive() {
        return getArchives()[0];
    }

    @Override
    public File getInstallFolder(String osSdkRoot, SdkManager sdkManager) {
        return new File(new File(osSdkRoot, "mock"), mTestHandle);
    }

    @Override
    public String installId() {
        return "mock-empty-" + mTestHandle;  //$NON-NLS-1$
    }

    @Override
    public String getListDescription() {
        String ld = super.getListDisplay();
        return ld.isEmpty() ? this.getClass().getSimpleName() : ld;
    }

    @Override
    public String getShortDescription() {
        StringBuilder sb = new StringBuilder(this.getClass().getSimpleName());
        sb.append(" '").append(mTestHandle).append('\'');
        if (getRevision().getMajor() > 0) {
            sb.append(" rev=").append(getRevision());
        }
        return sb.toString();
    }

    /** Returns true if these packages have the same handle. */
    @Override
    public boolean sameItemAs(Package pkg) {
        return (pkg instanceof MockEmptyPackage) &&
            mTestHandle.equals(((MockEmptyPackage) pkg).mTestHandle);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((mTestHandle == null) ? 0 : mTestHandle.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof MockEmptyPackage)) {
            return false;
        }
        MockEmptyPackage other = (MockEmptyPackage) obj;
        if (mTestHandle == null) {
            if (other.mTestHandle != null) {
                return false;
            }
        } else if (!mTestHandle.equals(other.mTestHandle)) {
            return false;
        }
        return true;
    }
}
