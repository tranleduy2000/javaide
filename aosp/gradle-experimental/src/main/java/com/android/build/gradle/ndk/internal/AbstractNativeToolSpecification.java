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

import static com.android.build.gradle.ndk.internal.BinaryToolHelper.getCCompiler;
import static com.android.build.gradle.ndk.internal.BinaryToolHelper.getCppCompiler;

import org.gradle.nativeplatform.NativeBinarySpec;

/**
 * An abstract class for NativeToolSpecification.
 */
public abstract class AbstractNativeToolSpecification implements NativeToolSpecification {
    /**
     * Configure a native binary with this specification.
     *
     * @param binary The binary to be configured.  It is assumed the 'c' and 'cpp' plugin is applied
     *               such that the binary contains the cCompiler and cppCompiler extensions.
     */
    @Override
    public void apply(NativeBinarySpec binary) {
        for (String arg : getCFlags()) {
            getCCompiler(binary).args(arg);
        }

        for (String arg : getCppFlags()) {
            getCppCompiler(binary).args(arg);
        }

        for (String arg : getLdFlags()) {
            binary.getLinker().args(arg);
        }
    }
}
