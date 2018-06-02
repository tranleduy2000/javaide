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
import com.android.sdklib.internal.androidTarget.MockAddonTarget;
import com.android.sdklib.internal.repository.sources.SdkSource;
import com.android.sdklib.repository.PkgProps;

import java.util.Properties;

/**
 * A mock {@link AddonPackage} for testing.
 *
 * By design, this package contains one and only one archive.
 */
public class MockAddonPackage extends AddonPackage {

    /**
     * Creates a {@link MockAddonTarget} with the requested base platform and addon revision
     * and then a {@link MockAddonPackage} wrapping it and a default name of "addon".
     *
     * By design, this package contains one and only one archive.
     */
    public MockAddonPackage(MockPlatformPackage basePlatform, int revision) {
        this("addon", basePlatform, revision); //$NON-NLS-1$
    }

    /**
     * Creates a {@link MockAddonTarget} with the requested base platform and addon revision
     * and then a {@link MockAddonPackage} wrapping it.
     *
     * By design, this package contains one and only one archive.
     */
    public MockAddonPackage(String name, MockPlatformPackage basePlatform, int revision) {
        super(new MockAddonTarget(name, basePlatform.getTarget(), revision), null /*props*/);
    }

    public MockAddonPackage(
            SdkSource source,
            String name,
            MockPlatformPackage basePlatform,
            int revision) {
        super(source,
              new MockAddonTarget(name, basePlatform.getTarget(), revision),
              createProperties(name, basePlatform.getTarget()));
    }

    private static Properties createProperties(String name, IAndroidTarget baseTarget) {
        String vendor = baseTarget.getVendor();
        Properties props = new Properties();
        props.setProperty(PkgProps.ADDON_NAME_ID, name);
        props.setProperty(PkgProps.ADDON_NAME_DISPLAY,
                String.format("The %1$s from %2$s",                  //$NON-NLS-1$
                        name, vendor));
        props.setProperty(PkgProps.ADDON_VENDOR_ID,
                String.format("vendor-id-%1$s", vendor));                   //$NON-NLS-1$
        props.setProperty(PkgProps.ADDON_VENDOR_DISPLAY,
                String.format("The %1$s", vendor));                  //$NON-NLS-1$
        return props;
    }
}
