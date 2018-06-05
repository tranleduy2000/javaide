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

package com.android.build.gradle.tasks.factory;

import com.android.build.gradle.internal.CompileOptions;
import com.android.build.gradle.internal.scope.ConventionMappingHelper;
import com.android.sdklib.AndroidTargetHash;
import com.android.sdklib.AndroidVersion;

import org.gradle.api.JavaVersion;
import org.gradle.api.tasks.compile.AbstractCompile;

import java.util.concurrent.Callable;

/**
 * Common code for configuring {@link AbstractCompile} instances.
 */
public class AbstractCompilesUtil {

    /**
     * Determines the java language level to use and sets it on the given task and
     * {@link CompileOptions}. The latter is to propagate the information to Studio.
     */
    public static void configureLanguageLevel(
            AbstractCompile compileTask,
            final CompileOptions compileOptions,
            String compileSdkVersion) {
        final AndroidVersion hash = AndroidTargetHash.getVersionFromHash(compileSdkVersion);
        Integer compileSdkLevel = (hash == null ? null : hash.getApiLevel());

        JavaVersion javaVersionToUse;
        if (compileSdkLevel == null || (0 <= compileSdkLevel && compileSdkLevel <= 20)) {
            javaVersionToUse = JavaVersion.VERSION_1_6;
        } else {
            javaVersionToUse = JavaVersion.VERSION_1_7;
        }

        JavaVersion jdkVersion =
                JavaVersion.toVersion(System.getProperty("java.specification.version"));
        if (jdkVersion.compareTo(javaVersionToUse) < 0) {
            compileTask.getLogger().warn(
                    "Default language level for compileSdkVersion '{}' is " +
                            "{}, but the JDK used is {}, so the JDK language level will be used.",
                    compileSdkVersion,
                    javaVersionToUse,
                    jdkVersion);
            javaVersionToUse = jdkVersion;
        }

        compileOptions.setDefaultJavaVersion(javaVersionToUse);

        compileTask.setSourceCompatibility(compileOptions.getSourceCompatibility().toString());
        compileTask.setTargetCompatibility(compileOptions.getTargetCompatibility().toString());
    }
}
