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

package com.android.build.gradle.tasks

import com.android.annotations.NonNull
import com.android.build.gradle.internal.dsl.CoreNdkOptions
import com.android.build.gradle.internal.tasks.NdkTask
import com.android.ide.common.process.LoggedProcessOutputHandler
import com.android.ide.common.process.ProcessInfoBuilder
import com.android.ide.common.process.ProcessOutputHandler
import com.android.sdklib.IAndroidTarget
import com.android.utils.FileUtils
import com.google.common.base.Charsets
import com.google.common.base.Joiner
import com.google.common.collect.Lists
import com.google.common.io.Files
import org.gradle.api.GradleException
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.gradle.api.tasks.util.PatternSet

import static com.android.SdkConstants.CURRENT_PLATFORM
import static com.android.SdkConstants.PLATFORM_WINDOWS

class NdkCompile extends NdkTask {

    public static String USE_DEPRECATED_NDK = "android.useDeprecatedNdk";

    List<File> sourceFolders

    @OutputFile
    File generatedMakefile

    @Input
    boolean debuggable

    @OutputDirectory
    File soFolder

    @OutputDirectory
    File objFolder

    @Optional
    @Input
    File ndkDirectory

    @Input
    boolean ndkRenderScriptMode

    @Input
    boolean ndkCygwinMode

    @Input
    boolean isForTesting

    @SkipWhenEmpty
    @InputFiles
    FileTree getSource() {
        FileTree src = null
        List<File> sources = getSourceFolders()
        if (!sources.isEmpty()) {
            src = getProject().files(new ArrayList<Object>(sources)).getAsFileTree()
        }
        return src == null ? getProject().files().getAsFileTree() : src
    }

    @TaskAction
    void taskAction(IncrementalTaskInputs inputs) {
         if (!project.hasProperty(USE_DEPRECATED_NDK)) {
             // Normally, we would catch the user when they try to configure the NDK, but NDK do
             // not need to be configured by default.  Throw this exception during task execution in
             // case we miss it.
             throw new RuntimeException(
                     "Error: NDK integration is deprecated in the current plugin.  Consider trying " +
                             "the new experimental plugin.  For details, see " +
                             "http://tools.android.com/tech-docs/new-build-system/gradle-experimental.  " +
                             "Set \"$USE_DEPRECATED_NDK=true\" in gradle.properties to " +
                             "continue using the current NDK integration.");
         }


        if (isNdkOptionUnset()) {
            logger.warn("Warning: Native C/C++ source code is found, but it seems that NDK " +
                    "option is not configured.  Note that if you have an Android.mk, it is not " +
                    "used for compilation.  The recommended workaround is to remove the default " +
                    "jni source code directory by adding: \n " +
                    "android {\n" +
                    "    sourceSets {\n" +
                    "        main {\n" +
                    "            jni.srcDirs = []\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n" +
                    "to build.gradle, manually compile the code with ndk-build, " +
                    "and then place the resulting shared object in src/main/jniLibs.");
        }

        FileTree sourceFileTree = getSource()
        Set<File> sourceFiles = sourceFileTree.matching(new PatternSet().exclude("**/*.h")).files
        File makefile = getGeneratedMakefile()

        if (sourceFiles.isEmpty()) {
            makefile.delete()
            FileUtils.emptyFolder(getSoFolder())
            FileUtils.emptyFolder(getObjFolder())
            return
        }

        if (ndkDirectory == null || !ndkDirectory.isDirectory()) {
            throw new GradleException(
                    "NDK not configured.\n" +
                    "Download the NDK from http://developer.android.com/tools/sdk/ndk/." +
                    "Then add ndk.dir=path/to/ndk in local.properties.\n" +
                    "(On Windows, make sure you escape backslashes, e.g. C:\\\\ndk rather than C:\\ndk)");
        }

        boolean generateMakefile = false

        if (!inputs.isIncremental()) {
            project.logger.info("Unable do incremental execution: full task run")
            generateMakefile = true
            FileUtils.emptyFolder(getSoFolder())
            FileUtils.emptyFolder(getObjFolder())
        } else {
            // look for added or removed files *only*

            //noinspection GroovyAssignabilityCheck
            inputs.outOfDate { change ->
                if (change.isAdded()) {
                    generateMakefile = true
                }
            }

            //noinspection GroovyAssignabilityCheck
            inputs.removed { change ->
                generateMakefile = true
            }
        }

        if (generateMakefile) {
            writeMakefile(sourceFiles, makefile)
        }

        // now build
        runNdkBuild(ndkDirectory, makefile)
    }

    private void writeMakefile(@NonNull Set<File> sourceFiles, @NonNull File makefile) {
        CoreNdkOptions ndk = getNdkConfig()

        StringBuilder sb = new StringBuilder()

        sb.append(
                'LOCAL_PATH := $(call my-dir)\n' +
                'include \$(CLEAR_VARS)\n\n')

        String moduleName = ndk.moduleName != null ? ndk.moduleName : project.name
        if (isForTesting) {
            moduleName = moduleName + "_test"
        }

        sb.append('LOCAL_MODULE := ').append(moduleName).append('\n')

        if (ndk.cFlags != null) {
            sb.append('LOCAL_CFLAGS := ').append(ndk.cFlags).append('\n')
        }

        // To support debugging from Android Studio.
        sb.append("LOCAL_LDFLAGS := -Wl,--build-id\n")

        List<String> fullLdlibs = Lists.newArrayList()
        if (ndk.ldLibs != null) {
            fullLdlibs.addAll(ndk.ldLibs)
        }
        if (getNdkRenderScriptMode()) {
            fullLdlibs.add("dl")
            fullLdlibs.add("log")
            fullLdlibs.add("jnigraphics")
            fullLdlibs.add("RScpp_static")
            fullLdlibs.add("cutils")
        }

        if (!fullLdlibs.isEmpty()) {
            sb.append('LOCAL_LDLIBS := \\\n')
            for (String lib : fullLdlibs) {
                sb.append('\t-l') .append(lib).append(' \\\n')
            }
            sb.append('\n')
        }

        sb.append('LOCAL_SRC_FILES := \\\n')
        for (File sourceFile : sourceFiles) {
            sb.append('\t').append(sourceFile.absolutePath).append(' \\\n')
        }
        sb.append('\n')

        for (File sourceFolder : getSourceFolders()) {
            sb.append("LOCAL_C_INCLUDES += ${sourceFolder.absolutePath}\n")
        }

        if (getNdkRenderScriptMode()) {
            sb.append('LOCAL_LDFLAGS += -L$(call host-path,$(TARGET_C_INCLUDES)/../lib/rs)\n')

            sb.append('LOCAL_C_INCLUDES += $(TARGET_C_INCLUDES)/rs/cpp\n')
            sb.append('LOCAL_C_INCLUDES += $(TARGET_C_INCLUDES)/rs\n')
            sb.append('LOCAL_C_INCLUDES += $(TARGET_OBJS)/$(LOCAL_MODULE)\n')
        }

        sb.append(
                '\ninclude \$(BUILD_SHARED_LIBRARY)\n')

        Files.write(sb.toString(), makefile, Charsets.UTF_8)
    }

    private void runNdkBuild(@NonNull File ndkLocation, @NonNull File makefile) {
        CoreNdkOptions ndk = getNdkConfig()

        ProcessInfoBuilder builder = new ProcessInfoBuilder()

        String exe = ndkLocation.absolutePath + File.separator + "ndk-build"
        if (CURRENT_PLATFORM == PLATFORM_WINDOWS && !ndkCygwinMode) {
            exe += ".cmd"
        }
        builder.setExecutable(exe)

        builder.addArgs(
                "NDK_PROJECT_PATH=null",
                "APP_BUILD_SCRIPT=" + makefile.absolutePath)

        // target
        IAndroidTarget target = getBuilder().getTarget()
        if (!target.isPlatform()) {
            target = target.parent
        }
        builder.addArgs("APP_PLATFORM=" + target.hashString())

        // temp out
        builder.addArgs("NDK_OUT=" + getObjFolder().absolutePath)

        // libs out
        builder.addArgs("NDK_LIBS_OUT=" + getSoFolder().absolutePath)

        // debug builds
        if (getDebuggable()) {
            builder.addArgs("NDK_DEBUG=1")
        }

        if (ndk.getStl() != null) {
            builder.addArgs("APP_STL=" + ndk.getStl())
        }

        Set<String> abiFilters = ndk.abiFilters
        if (abiFilters != null && !abiFilters.isEmpty()) {
            if (abiFilters.size() == 1) {
                builder.addArgs("APP_ABI=" + abiFilters.iterator().next())
            } else {
                Joiner joiner = Joiner.on(',').skipNulls()
                builder.addArgs("APP_ABI=" + joiner.join(abiFilters.iterator()))
            }
        } else {
            builder.addArgs("APP_ABI=all")
        }

        if (ndk.getJobs() != null) {
            builder.addArgs("-j" + ndk.getJobs());
        }

        ProcessOutputHandler handler = new LoggedProcessOutputHandler(getBuilder().getLogger());
        getBuilder().executeProcess(builder.createProcess(), handler)
                .rethrowFailure().assertNormalExitValue()
    }

    private boolean isNdkOptionUnset() {
        // If none of the NDK options are set, then it is likely that NDK is not configured.
        return (getModuleName() == null &&
                getcFlags() == null &&
                getLdLibs() == null &&
                getAbiFilters() == null &&
                getStl() == null);
    }
}
