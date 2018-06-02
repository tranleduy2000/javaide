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
import com.android.sdklib.internal.repository.sources.SdkSource;


/**
 * A mock {@link MockSourcePackage} for testing.
 *
 * By design, this package contains one and only one archive.
 */
public class MockSourcePackage extends SourcePackage {
    /**
     * Creates a {@link MockSourcePackage} using the given base platform version
     * and package revision.
     *
     * By design, this package contains one and only one archive.
     */
    public MockSourcePackage(AndroidVersion version, int revision) {
        super(version,
                revision,
                null /*props*/,
                String.format("/sdk/sources/android-%s", version.getApiString()));
    }

    /**
     * Creates a {@link MockSourcePackage} using the given version,
     * sdk source and package revision.
     *
     * By design, this package contains one and only one archive.
     */
    public MockSourcePackage(
            SdkSource source,
            AndroidVersion version,
            int revision) {
        super(source,
                version,
                revision,
                null /*props*/,
                String.format("/sdk/sources/android-%s", version.getApiString()));
    }
}
