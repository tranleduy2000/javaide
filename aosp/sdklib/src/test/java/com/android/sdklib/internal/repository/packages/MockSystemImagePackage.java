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


/**
 * A mock {@link SystemImagePackage} for testing.
 *
 * By design, this package contains one and only one archive.
 */
public class MockSystemImagePackage extends SystemImagePackage {
    /**
     * Creates a {@link MockSystemImagePackage} using the given base platform version
     * and package revision.
     *
     * By design, this package contains one and only one archive.
     */
    public MockSystemImagePackage(MockPlatformPackage basePlatform, int revision, String abi) {
        super(basePlatform.getAndroidVersion(),
                revision,
                abi,
                null /*props*/,
                String.format("/sdk/system-images/android-%s/%s",
                        basePlatform.getAndroidVersion().getApiString(), abi));
    }

    /**
     * Creates a {@link MockSystemImagePackage} using the given base platform version,
     * sdk source and package revision.
     *
     * By design, this package contains one and only one archive.
     */
    public MockSystemImagePackage(
            SdkSource source,
            MockPlatformPackage basePlatform,
            int revision,
            String abi) {
        super(source,
                basePlatform.getAndroidVersion(),
                revision,
                abi,
                null /*props*/,
                String.format("/sdk/system-images/android-%s/%s",
                        basePlatform.getAndroidVersion().getApiString(), abi));
    }
}
