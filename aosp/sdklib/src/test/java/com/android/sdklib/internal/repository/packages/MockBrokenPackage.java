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

import com.android.sdklib.repository.FullRevision;
import com.android.sdklib.repository.descriptors.PkgDesc;


/**
 * A mock {@link BrokenPackage} for testing.
 * <p/>
 * By design, this package contains one and only one archive.
 */
public class MockBrokenPackage extends BrokenPackage {

    public MockBrokenPackage(int minApiLevel, int exactApiLevel) {
        this(createDesription(minApiLevel, exactApiLevel),  // short description
             createDesription(minApiLevel, exactApiLevel),  // long description
             minApiLevel,
             exactApiLevel);
    }

    private static String createDesription(int minApiLevel, int exactApiLevel) {
        String s = "Broken package";
        s += exactApiLevel == BrokenPackage.API_LEVEL_INVALID ? " (No API level)" :
                String.format(" for API %d", exactApiLevel);
        s += minApiLevel == BrokenPackage.MIN_API_LEVEL_NOT_SPECIFIED ? "" :
                String.format(", min API %d", minApiLevel);
        return s;
    }

    public MockBrokenPackage(
            String shortDescription,
            String longDescription,
            int minApiLevel,
            int exactApiLevel) {
        super(null /*props*/,
                shortDescription,
                longDescription,
                minApiLevel,
                exactApiLevel,
                "/sdk/broken/package" /*osPath*/,
                PkgDesc.Builder.newTool(
                        new FullRevision(1, 2, 3, 4),
                        FullRevision.NOT_SPECIFIED).create());
    }
}
