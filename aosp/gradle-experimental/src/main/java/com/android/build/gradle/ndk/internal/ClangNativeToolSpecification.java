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

import com.android.SdkConstants;
import com.android.build.gradle.internal.NdkHandler;
import com.android.build.gradle.internal.core.Abi;
import com.android.builder.core.BuilderConstants;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;

import org.gradle.nativeplatform.BuildType;
import org.gradle.nativeplatform.platform.NativePlatform;

import java.util.Map;

/**
 * Flag configuration for Clang toolchain.
 */
public class ClangNativeToolSpecification extends AbstractNativeToolSpecification {

    private NdkHandler ndkHandler;

    private NativePlatform platform;

    private boolean isDebugBuild;

    private static final Map<String, String> TARGET_TRIPLE = ImmutableMap.<String, String>builder()
                .put(SdkConstants.ABI_INTEL_ATOM, "i686-none-linux-android")
                .put(SdkConstants.ABI_INTEL_ATOM64, "x86_64-none-linux-android")
                .put(SdkConstants.ABI_ARMEABI, "armv5-none-linux-android")
                .put(SdkConstants.ABI_ARMEABI_V7A, "armv7-none-linux-android")
                .put(SdkConstants.ABI_ARM64_V8A, "aarch64-none-linux-android")
                .put(SdkConstants.ABI_MIPS, "mipsel-none-linux-android")
                .put(SdkConstants.ABI_MIPS64, "mips64el-none-linux-android")
                .build();


    private static final ListMultimap<String, String> RELEASE_CFLAGS =
            ImmutableListMultimap.<String, String>builder()
                    .putAll(SdkConstants.ABI_ARMEABI, ImmutableList.of(
                            "-fpic",
                            "-ffunction-sections",
                            "-funwind-tables",
                            "-fstack-protector",
                            "-no-canonical-prefixes",
                            "-march=armv5te",
                            "-mtune=xscale",
                            "-msoft-float",
                            "-mthumb",
                            "-Os",
                            "-DNDEBUG",
                            "-fomit-frame-pointer",
                            "-fstrict-aliasing"))
                    .putAll(SdkConstants.ABI_ARMEABI_V7A, ImmutableList.of(
                            "-fpic",
                            "-ffunction-sections",
                            "-funwind-tables",
                            "-fstack-protector",
                            "-no-canonical-prefixes",
                            "-march=armv7-a",
                            "-mfloat-abi=softfp",
                            "-mfpu=vfpv3-d16",
                            "-mthumb",
                            "-Os",
                            "-DNDEBUG",
                            "-fomit-frame-pointer",
                            "-fstrict-aliasing"))
                    .putAll(SdkConstants.ABI_ARM64_V8A, ImmutableList.of(
                            "-fpic",
                            "-ffunction-sections",
                            "-funwind-tables",
                            "-fstack-protector",
                            "-no-canonical-prefixes",
                            "-O2",
                            "-DNDEBUG",
                            "-fomit-frame-pointer",
                            "-fstrict-aliasing"))
                    .putAll(SdkConstants.ABI_INTEL_ATOM, ImmutableList.of(
                            "-ffunction-sections",
                            "-funwind-tables",
                            "-fstack-protector",
                            "-fPIC",
                            "-no-canonical-prefixes",
                            "-O2",
                            "-DNDEBUG",
                            "-fomit-frame-pointer",
                            "-fstrict-aliasing"))
                    .putAll(SdkConstants.ABI_INTEL_ATOM64, ImmutableList.of(
                            "-ffunction-sections",
                            "-funwind-tables",
                            "-fstack-protector",
                            "-fPIC",
                            "-no-canonical-prefixes",
                            "-O2",
                            "-DNDEBUG",
                            "-fomit-frame-pointer",
                            "-fstrict-aliasing"))
                    .putAll(SdkConstants.ABI_MIPS, ImmutableList.of(
                            "-fpic",
                            "-fno-strict-aliasing",
                            "-finline-functions",
                            "-ffunction-sections",
                            "-funwind-tables",
                            "-fmessage-length=0",
                            "-no-canonical-prefixes",
                            "-O2",
                            "-g",
                            "-DNDEBUG",
                            "-fomit-frame-pointer"))
                    .putAll(SdkConstants.ABI_MIPS64, ImmutableList.of(
                            "-fpic",
                            "-fno-strict-aliasing",
                            "-finline-functions",
                            "-ffunction-sections",
                            "-funwind-tables",
                            "-fmessage-length=0",
                            "-no-canonical-prefixes",
                            "-O2",
                            "-g",
                            "-DNDEBUG",
                            "-fomit-frame-pointer"))
                    .build();

    private static final ListMultimap<String, String> DEBUG_CFLAGS =
            ImmutableListMultimap.<String, String>builder()
                    .putAll(SdkConstants.ABI_ARMEABI, ImmutableList.of(
                            "-O0",
                            "-UNDEBUG",
                            "-marm",
                            "-fno-strict-aliasing",
                            "-fno-limit-debug-info"))
                    .putAll(SdkConstants.ABI_ARMEABI_V7A, ImmutableList.of(
                            "-O0",
                            "-UNDEBUG",
                            "-marm",
                            "-fno-strict-aliasing",
                            "-fno-limit-debug-info"))
                    .putAll(SdkConstants.ABI_ARM64_V8A, ImmutableList.of(
                            "-O0",
                            "-UNDEBUG",
                            "-fno-omit-frame-pointer",
                            "-fno-strict-aliasing",
                            "-fno-limit-debug-info"))
                    .putAll(SdkConstants.ABI_INTEL_ATOM, ImmutableList.of(
                            "-O0",
                            "-UNDEBUG",
                            "-fno-omit-frame-pointer",
                            "-fno-strict-aliasing",
                            "-fno-limit-debug-info"))
                    .putAll(SdkConstants.ABI_INTEL_ATOM64, ImmutableList.of(
                            "-O0",
                            "-UNDEBUG",
                            "-fno-omit-frame-pointer",
                            "-fno-strict-aliasing",
                            "-fno-limit-debug-info"))
                    .putAll(SdkConstants.ABI_MIPS, ImmutableList.of(
                            "-O0",
                            "-UNDEBUG",
                            "-fno-omit-frame-pointer",
                            "-fno-limit-debug-info"))
                    .putAll(SdkConstants.ABI_MIPS64, ImmutableList.of(
                            "-O0",
                            "-UNDEBUG",
                            "-fno-omit-frame-pointer",
                            "-fno-limit-debug-info"))
                    .build();

    public ClangNativeToolSpecification(
            NdkHandler ndkHandler,
            NativePlatform platform,
            boolean isDebugBuild) {
        this.ndkHandler = ndkHandler;
        this.isDebugBuild = isDebugBuild;
        this.platform = platform;
    }

    @Override
    public Iterable<String> getCFlags() {
        return Iterables.concat(
                getTargetFlags(),
                RELEASE_CFLAGS.get(platform.getName()),
                isDebugBuild ? DEBUG_CFLAGS.get(platform.getName()) : ImmutableList.<String>of());
    }

    @Override
    public Iterable<String> getCppFlags() {
        return getCFlags();
    }

    @Override
    public Iterable<String> getLdFlags() {
        return Iterables.concat(
                getTargetFlags(),
                platform.getName().equals(SdkConstants.ABI_ARMEABI_V7A)
                        ? ImmutableList.of("-Wl,--fix-cortex-a8")
                        : ImmutableList.<String>of());
    }

    private Iterable<String> getTargetFlags() {
        return ImmutableList.of(
                "-gcc-toolchain",
                ndkHandler.getDefaultGccToolchainPath(Abi.getByName(platform.getName())).toString(),
                "-target",
                TARGET_TRIPLE.get(platform.getName()));
    }
}
