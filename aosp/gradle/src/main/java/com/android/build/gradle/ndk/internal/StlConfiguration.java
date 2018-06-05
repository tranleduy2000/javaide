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

import com.android.annotations.NonNull;
import com.android.build.gradle.internal.NdkHandler;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;

import org.gradle.api.Action;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Task;
import org.gradle.api.tasks.Copy;
import org.gradle.model.ModelMap;
import org.gradle.nativeplatform.NativeBinarySpec;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * Configuration to setup STL for NDK.
 */
public class StlConfiguration {

    private static final String DEFAULT_STL = "system";

    private static final List<String> VALID_STL = ImmutableList.of(
            "system",
            "stlport_static",
            "stlport_shared",
            "gnustl_static",
            "gnustl_shared",
            "gabi++_static",
            "gabi++_shared",
            "c++_static",
            "c++_shared");

    private static final ListMultimap<String, String> STL_SOURCES =
            ImmutableListMultimap.<String, String>builder()
                    .putAll("system", ImmutableList.of(
                            "system/include"))
                    .putAll("stlport", ImmutableList.of(
                            "stlport/stlport",
                            "gabi++/include"))
                    .putAll("gnustl", ImmutableList.of(
                            "gnu-libstdc++",
                            "gnu-libstdc++/4.6/include",
                            "gnu-libstdc++/4.6/libs/armeabi-v7a/include",
                            "gnu-libstdc++/4.6/include/backward"))
                    .putAll("gabi++", ImmutableList.of(
                            "gabi++",
                            "gabi++/include"))
                    .putAll("c++", ImmutableList.of(
                            "../android/support/include",
                            "llvm-libc++",
                            "../android/compiler-rt",
                            "llvm-libc++/libcxx/include",
                            "gabi++/include",
                            "../android/support/include"))
                    .build();

    public static File getStlBaseDirectory(NdkHandler ndkHandler) {
        return new File(ndkHandler.getNdkDirectory(), "sources/cxx-stl/");
    }

    public static Collection<String> getStlSources(NdkHandler ndkHandler, String stl) {
        final File stlBase = getStlBaseDirectory(ndkHandler);
        String stlName = stl.equals("system") ? "system" : stl.substring(0, stl.indexOf('_'));

        ImmutableList.Builder<String> builder = ImmutableList.builder();
        for (String sourceDir : STL_SOURCES.get(stlName)) {
            builder.add(stlBase.toString() + "/" + sourceDir);
        }
        return builder.build();
    }

    public static void checkStl(String stl) {
        if (!VALID_STL.contains(stl)) {
            throw new InvalidUserDataException("Invalid STL: " + stl);
        }
    }

    public static void createStlCopyTask(
            @NonNull ModelMap<Task> tasks,
            @NonNull final NativeBinarySpec binary,
            @NonNull final File buildDir,
            @NonNull NdkHandler ndkHandler,
            @NonNull String stl,
            @NonNull String buildTaskName) {
        if (stl.endsWith("_shared")) {
            final StlNativeToolSpecification stlConfig = new StlNativeToolSpecification(ndkHandler,
                    stl, binary.getTargetPlatform());

            final String copyTaskName = NdkNamingScheme.getTaskName(binary, "copy", "StlSo");
            tasks.create(copyTaskName, Copy.class, new Action<Copy>() {
                @Override
                public void execute(Copy copy) {
                    copy.from(stlConfig.getStlLib(binary.getTargetPlatform().getName()));
                    copy.into(new File(buildDir, NdkNamingScheme.getOutputDirectoryName(binary)));

                }
            });
            tasks.named(buildTaskName, new Action<Task>() {
                @Override
                public void execute(Task task) {
                    task.dependsOn(copyTaskName);
                }
            });
        }
    }
}
