/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.build.gradle.internal;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.google.common.base.Charsets;
import org.gradle.api.JavaVersion;

import java.util.Locale;

/**
 * Compilation options.
 */
public class CompileOptions {
    @Nullable
    private JavaVersion sourceCompatibility;

    @Nullable
    private JavaVersion targetCompatibility;

    private String encoding = Charsets.UTF_8.name();

    /**
     * Default Java version that will be used if the source and target compatibility levels will
     * not be set explicitly.
     */
    private JavaVersion defaultJavaVersion = JavaVersion.VERSION_1_6;

    private boolean ndkCygwinMode = false;

    /**
     * Language level of the source code.
     *
     * <p>Formats supported are :
     *      "1.6"
     *      1.6
     *      JavaVersion.Version_1_6
     *      "Version_1_6"
     */
    public void setSourceCompatibility(@NonNull Object sourceCompatibility) {
        this.sourceCompatibility = convert(sourceCompatibility);
    }

    /**
     * Language level of the source code.
     *
     * <p>Similar to what <a href="http://www.gradle.org/docs/current/userguide/java_plugin.html">
     * Gradle Java plugin</a> uses.
     */
    @NonNull
    public JavaVersion getSourceCompatibility() {
        return sourceCompatibility != null ? sourceCompatibility : defaultJavaVersion;
    }

    /**
     * Language level of the target code.
     *
     * <p>Formats supported are :
     *      "1.6"
     *      1.6
     *      JavaVersion.Version_1_6
     *      "Version_1_6"
     */
    public void setTargetCompatibility(@NonNull Object targetCompatibility) {
        this.targetCompatibility = convert(targetCompatibility);
    }

    /**
     * Version of the generated Java bytecode.
     *
     * <p>Similar to what <a href="http://www.gradle.org/docs/current/userguide/java_plugin.html">
     * Gradle Java plugin</a> uses.
     */
    @NonNull
    public JavaVersion getTargetCompatibility() {
        return targetCompatibility != null ? targetCompatibility : defaultJavaVersion;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setDefaultJavaVersion(JavaVersion defaultJavaVersion) {
        this.defaultJavaVersion = defaultJavaVersion;
    }

    public JavaVersion getDefaultJavaVersion() {
        return defaultJavaVersion;
    }


    private static final String VERSION_PREFIX = "VERSION_";
    /**
     * Convert all possible supported way of specifying a Java version to {@link JavaVersion}
     * @param version the user provided java version.
     * @return {@link JavaVersion}
     * @throws RuntimeException if it cannot be converted.
     */
    @NonNull
    private static JavaVersion convert(@NonNull Object version) {
        // for backward version reasons, we support setting strings like 'Version_1_6'
        if (version instanceof String) {
            final String versionString = (String) version;
            if (versionString.toUpperCase(Locale.ENGLISH).startsWith(VERSION_PREFIX)) {
                version = versionString.substring(VERSION_PREFIX.length()).replace('_', '.');
            }
        }
        return JavaVersion.toVersion(version);
    }
}
