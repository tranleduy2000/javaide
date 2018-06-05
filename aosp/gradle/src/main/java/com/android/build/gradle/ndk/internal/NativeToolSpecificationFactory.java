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

package com.android.build.gradle.ndk.internal;

import com.android.build.gradle.internal.NdkHandler;
import com.android.build.gradle.internal.core.Toolchain;

import org.gradle.nativeplatform.BuildType;
import org.gradle.nativeplatform.platform.NativePlatform;

/**
 * Factory to create a NativeToolSpecification.
 */
public class NativeToolSpecificationFactory {

    /**
     * Returns a NativeToolSpecification.
     *
     * @param platform  Target platform of the native binary.
     * @param isDebugBuild Is the build debuggable.
     * @return A NativeToolSpecification for the targeted native binary.
     */
    public static NativeToolSpecification create(
            NdkHandler ndkHandler,
            NativePlatform platform,
            boolean isDebugBuild) {
        return (ndkHandler.getToolchain().equals(Toolchain.GCC)
                ? new GccNativeToolSpecification(platform, isDebugBuild)
                : new ClangNativeToolSpecification(ndkHandler, platform, isDebugBuild));
    }

}
