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

package com.android.build.gradle.ndk.internal;

import org.gradle.api.plugins.ExtensionAware;
import org.gradle.language.nativeplatform.internal.DefaultPreprocessingTool;
import org.gradle.platform.base.BinarySpec;

/**
 * Gradle's LanguageRegistration dynamically add the cCompiler and cppCompiler tool to the library.
 *
 * In Java, we can't call those functions dynamically, but can cast the binaries to ExternsionAware
 * and access those tools through the extension container.
 *
 * This helper class hide the details for accessing the tools.
 */
public class BinaryToolHelper {
    public static DefaultPreprocessingTool getCCompiler(BinarySpec binary) {
        return (DefaultPreprocessingTool) ((ExtensionAware) binary).getExtensions().getByName("cCompiler");
    }

    public static DefaultPreprocessingTool getCppCompiler(BinarySpec binary) {
        return (DefaultPreprocessingTool) ((ExtensionAware) binary).getExtensions().getByName("cppCompiler");
    }
}
