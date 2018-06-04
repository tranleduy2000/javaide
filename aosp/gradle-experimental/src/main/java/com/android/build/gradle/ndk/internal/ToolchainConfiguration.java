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
import com.android.build.gradle.internal.core.Abi;
import com.android.build.gradle.internal.core.Toolchain;

import org.gradle.api.Action;
import org.gradle.nativeplatform.platform.NativePlatform;
import org.gradle.nativeplatform.toolchain.Clang;
import org.gradle.nativeplatform.toolchain.Gcc;
import org.gradle.nativeplatform.toolchain.GccCompatibleToolChain;
import org.gradle.nativeplatform.toolchain.GccPlatformToolChain;
import org.gradle.nativeplatform.toolchain.NativeToolChainRegistry;
import org.gradle.platform.base.PlatformContainer;

import java.util.Collections;
import java.util.List;

/**
 * Action to configure toolchain for native binaries.
 */
public class ToolchainConfiguration {

    public static void configurePlatforms(PlatformContainer platforms, NdkHandler ndkHandler) {
        for (Abi abi : ndkHandler.getSupportedAbis()) {
            NativePlatform platform = platforms.maybeCreate(abi.getName(), NativePlatform.class);

            // All we care is the name of the platform.  It doesn't matter what the
            // architecture is, but it must be set to non-x86 so that it does not match
            // the default supported platform.
            platform.architecture("ppc");
            platform.operatingSystem("linux");
        }

    }

    /**
     * Configure toolchain for a platform.
     */
    public static void configureToolchain(
            NativeToolChainRegistry toolchainRegistry,
            final String toolchainName,
            final NdkHandler ndkHandler) {
        final Toolchain ndkToolchain = Toolchain.getByName(toolchainName);
        toolchainRegistry.create("ndk-" + toolchainName,
                toolchainName.equals("gcc") ? Gcc.class : Clang.class,
                new Action<GccCompatibleToolChain>() {
                    @Override
                    public void execute(GccCompatibleToolChain toolchain) {
                        // Configure each platform.
                        for (Abi it : ndkHandler.getSupportedAbis()) {
                            final Abi abi = it;
                            toolchain.target(abi.getName(), new Action<GccPlatformToolChain>() {
                                @Override
                                public void execute(GccPlatformToolChain targetPlatform) {
                                    if (Toolchain.GCC.equals(ndkToolchain)) {
                                        String gccPrefix = abi.getGccExecutablePrefix();
                                        targetPlatform.getcCompiler()
                                                .setExecutable(gccPrefix + "-gcc");
                                        targetPlatform.getCppCompiler()
                                                .setExecutable(gccPrefix + "-g++");
                                        targetPlatform.getLinker()
                                                .setExecutable(gccPrefix + "-g++");
                                        targetPlatform.getAssembler()
                                                .setExecutable(gccPrefix + "-as");
                                        targetPlatform.getStaticLibArchiver()
                                                .setExecutable(gccPrefix + "-ar");
                                    }

                                    // By default, gradle will use -Xlinker to pass arguments to the linker.
                                    // Removing it as it prevents -sysroot from being properly set.
                                    targetPlatform.getLinker().withArguments(
                                            new Action<List<String>>() {
                                                @Override
                                                public void execute(List<String> args) {
                                                    args.removeAll(Collections.singleton("-Xlinker"));
                                                }
                                            });
                                }

                            });
                            toolchain.path(ndkHandler.getCCompiler(abi).getParentFile());
                        }
                    }
                });
    }
}
