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

import com.android.sdklib.internal.repository.sources.SdkSource;
import com.android.sdklib.repository.PkgProps;

import java.util.Properties;

/**
 * A mock {@link ExtraPackage} for testing.
 *
 * By design, this package contains one and only one archive.
 */
public class MockExtraPackage extends ExtraPackage {

    /**
     * Creates a {@link MockExtraPackage} with the given revision and hardcoded defaults
     * for everything else.
     * <p/>
     * By design, this creates a package with one and only one archive.
     */
    public MockExtraPackage(String vendor, String path, int revision, int min_platform_tools_rev) {
        this(null /*source*/, vendor, path, revision, min_platform_tools_rev);
    }

    public MockExtraPackage(
            SdkSource source,
            Properties props,
            String vendor,
            String path,
            int revision,
            String license,
            String description,
            String descUrl,
            String archiveOsPath) {
        super(
            source,
            props,
            vendor,
            path,
            revision,
            license,
            description,
            descUrl,
            archiveOsPath);
    }

    public MockExtraPackage(
            SdkSource source,
            Properties props,
            String vendor,
            String path,
            int revision) {
        super(
            source,
            props, // props,
            vendor,
            path,
            revision,
            null, // license,
            "desc", // description,
            "url", // descUrl,
            source == null ? "foo" : null // archiveOsPath, null for remote non-instaled pkgs
            );
    }

    public MockExtraPackage(
            SdkSource source,
            String vendor,
            String path,
            int revision,
            int min_platform_tools_rev) {
        this(source, createProps(min_platform_tools_rev), vendor, path, revision);
    }

    private static Properties createProps(int min_platform_tools_rev) {
        Properties props = new Properties();
        props.setProperty(PkgProps.MIN_PLATFORM_TOOLS_REV,
                          Integer.toString((min_platform_tools_rev)));
        return props;
    }
}
