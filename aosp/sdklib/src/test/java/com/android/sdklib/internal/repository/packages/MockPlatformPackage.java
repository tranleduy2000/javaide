/*
 * Copyright (C) 2009 The Android Open Source Project
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

import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.internal.androidTarget.MockPlatformTarget;
import com.android.sdklib.internal.repository.sources.SdkSource;
import com.android.sdklib.repository.FullRevision;
import com.android.sdklib.repository.PkgProps;

import java.util.Properties;

/**
 * A mock {@link PlatformPackage} for testing.
 *
 * By design, this package contains one and only one archive.
 */
public class MockPlatformPackage extends PlatformPackage {

    private final IAndroidTarget mTarget;

    /**
     * Creates a {@link MockPlatformTarget} with the requested API and revision
     * and then a {@link MockPlatformPackage} wrapping it.
     *
     * By design, this package contains one and only one archive.
     */
    public MockPlatformPackage(int apiLevel, int revision) {
        this(null /*source*/, new MockPlatformTarget(apiLevel, revision), null /*props*/);
    }

    /**
     * Creates a {@link MockPlatformTarget} with the requested API and revision
     * and then a {@link MockPlatformPackage} wrapping it.
     *
     * Also sets the min-tools-rev of the platform using a major revision integer.
     *
     * By design, this package contains one and only one archive.
     */
    public MockPlatformPackage(int apiLevel, int revision, int min_tools_rev) {
        this(null /*source*/,
             new MockPlatformTarget(apiLevel, revision),
             createProps(min_tools_rev));
    }

    /**
     * Creates a {@link MockPlatformTarget} with the requested API and revision
     * and then a {@link MockPlatformPackage} wrapping it.
     *
     * Also sets the min-tools-rev of the platform using a {@link FullRevision}.
     *
     * By design, this package contains one and only one archive.
     */
    public MockPlatformPackage(int apiLevel, int revision, FullRevision min_tools_rev) {
        this(null /*source*/,
             new MockPlatformTarget(apiLevel, revision),
             createProps(min_tools_rev));
    }

    public MockPlatformPackage(SdkSource source, int apiLevel, int revision, int min_tools_rev) {
        this(source, new MockPlatformTarget(apiLevel, revision), createProps(min_tools_rev));
    }

    /** A little trick to be able to capture the target new after passing it to the super. */
    private MockPlatformPackage(SdkSource source, IAndroidTarget target, Properties props) {
        super(source, target, props);
        mTarget = target;
    }

    private static Properties createProps(int min_tools_rev) {
        Properties props = new Properties();
        props.setProperty(PkgProps.MIN_TOOLS_REV, Integer.toString((min_tools_rev)));
        return props;
    }

    private static Properties createProps(FullRevision min_tools_rev) {
        Properties props = new Properties();
        props.setProperty(PkgProps.MIN_TOOLS_REV, min_tools_rev.toShortString());
        return props;
    }

    public IAndroidTarget getTarget() {
        return mTarget;
    }
}
