/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.build.gradle.internal.core;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.annotations.Nullable;

/**
 * Enum of valid ABI you can specify for NDK.
 */
public enum Abi {
    //          name                           architecture                        gccToolchainPrefix        gccExecutablePrefix       supports64Bits
    ARMEABI    (SdkConstants.ABI_ARMEABI,      SdkConstants.CPU_ARCH_ARM,          "arm-linux-androideabi",  "arm-linux-androideabi",  false),
    ARMEABI_V7A(SdkConstants.ABI_ARMEABI_V7A,  SdkConstants.CPU_ARCH_ARM,          "arm-linux-androideabi",  "arm-linux-androideabi",  false),
    ARM64_V8A  (SdkConstants.ABI_ARM64_V8A,    SdkConstants.CPU_ARCH_ARM64,        "aarch64-linux-android",  "aarch64-linux-android",  true),
    X86        (SdkConstants.ABI_INTEL_ATOM,   SdkConstants.CPU_ARCH_INTEL_ATOM,   "x86",                    "i686-linux-android",     false),
    X86_64     (SdkConstants.ABI_INTEL_ATOM64, SdkConstants.CPU_ARCH_INTEL_ATOM64, "x86_64",                 "x86_64-linux-android",   true),
    MIPS       (SdkConstants.ABI_MIPS,         SdkConstants.CPU_ARCH_MIPS,         "mipsel-linux-android",   "mipsel-linux-android",   false),
    MIPS64     (SdkConstants.ABI_MIPS64,       SdkConstants.CPU_ARCH_MIPS64,       "mips64el-linux-android", "mips64el-linux-android", true);


    @NonNull
    private String name;
    @NonNull
    String architecture;
    @NonNull
    private String gccToolchainPrefix;
    @NonNull
    private String gccExecutablePrefix;
    private boolean supports64Bits;

    Abi(@NonNull String name, @NonNull String architecture,
            @NonNull String gccToolchainPrefix, @NonNull String gccExecutablePrefix, boolean supports64Bits) {
        this.name = name;
        this.architecture = architecture;
        this.gccToolchainPrefix = gccToolchainPrefix;
        this.gccExecutablePrefix = gccExecutablePrefix;
        this.supports64Bits = supports64Bits;
    }

    /**
     * Returns the ABI Enum with the specified name.
     */
    @Nullable
    public static Abi getByName(@NonNull String name) {
        for (Abi abi : values()) {
            if (abi.name.equals(name)) {
                return abi;
            }
        }
        return null;
    }

    /**
     * Returns name of the ABI.
     */
    @NonNull
    public String getName() {
        return name;
    }

    /**
     * Returns the CPU architecture.
     */
    @NonNull
    public String getArchitecture() {
        return architecture;
    }

    /**
     * Returns the platform string for locating the toolchains in the NDK.
     */
    @NonNull
    public String getGccToolchainPrefix() {
        return gccToolchainPrefix;
    }

    /**
     * Returns the prefix of GCC tools for the ABI.
     */
    @NonNull
    public String getGccExecutablePrefix() {
        return gccExecutablePrefix;
    }

    /**
     * Returns whether the ABI supports 64-bits.
     */
    public boolean supports64Bits() {
        return supports64Bits;
    }
}

