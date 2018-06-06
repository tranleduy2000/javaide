package com.android.build.gradle.tasks;

import com.android.annotations.NonNull;
import com.android.build.gradle.internal.dsl.CoreNdkOptions;
import com.android.build.gradle.internal.tasks.NdkTask;
import com.android.ide.common.process.LoggedProcessOutputHandler;
import com.android.ide.common.process.ProcessInfoBuilder;
import com.android.ide.common.process.ProcessOutputHandler;
import com.android.sdklib.IAndroidTarget;
import com.android.utils.FileUtils;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import org.gradle.api.GradleException;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.incremental.IncrementalTaskInputs;
import org.gradle.api.tasks.incremental.InputFileDetails;
import org.gradle.api.tasks.util.PatternSet;

import java.io.File;
import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NdkCompile extends NdkTask {
    public static String USE_DEPRECATED_NDK = "android.useDeprecatedNdk";
    private List<File> sourceFolders;
    @OutputFile
    private File generatedMakefile;
    @Input
    private boolean debuggable;
    @OutputDirectory
    private File soFolder;
    @OutputDirectory
    private File objFolder;
    @Optional
    @Input
    private File ndkDirectory;
    @Input
    private boolean ndkCygwinMode;

    private static <T> T setGroovyRef(groovy.lang.Reference<T> ref, T newValue) {
        ref.set(newValue);
        return newValue;
    }

    @SkipWhenEmpty
    @InputFiles
    public FileTree getSource() {
        FileTree src = null;
        List<File> sources = getSourceFolders();
        if (!sources.isEmpty().asBoolean()) {
            src = getProject().files(new ArrayList<Object>(sources)).getAsFileTree();
        }

        return src == null ? getProject().files().getAsFileTree() : src;
    }

    @TaskAction
    public void taskAction(IncrementalTaskInputs inputs) {
        if (!getProject().hasProperty(USE_DEPRECATED_NDK).asBoolean()) {
            // Normally, we would catch the user when they try to configure the NDK, but NDK do
            // not need to be configured by default.  Throw this exception during task execution in
            // case we miss it.
            throw new RuntimeException("Error: NDK integration is deprecated in the current plugin.  Consider trying " + "the new experimental plugin.  For details, see " + "http://tools.android.com/tech-docs/new-build-system/gradle-experimental.  " + "Set \"" + USE_DEPRECATED_NDK + "=true\" in gradle.properties to " + "continue using the current NDK integration.");
        }


        if (isNdkOptionUnset()) {
            getLogger().warn("Warning: Native C/C++ source code is found, but it seems that NDK " + "option is not configured.  Note that if you have an Android.mk, it is not " + "used for compilation.  The recommended workaround is to remove the default " + "jni source code directory by adding: \n " + "android {\n" + "    sourceSets {\n" + "        main {\n" + "            jni.srcDirs = []\n" + "        }\n" + "    }\n" + "}\n" + "to build.gradle, manually compile the code with ndk-build, " + "and then place the resulting shared object in src/main/jniLibs.");
        }


        FileTree sourceFileTree = getSource();
        Set<File> sourceFiles = sourceFileTree.matching(new PatternSet().exclude("**/*.h")).getFiles();
        File makefile = getGeneratedMakefile();

        if (sourceFiles.isEmpty()) {
            makefile.delete();
            FileUtils.emptyFolder(getSoFolder());
            FileUtils.emptyFolder(getObjFolder());
            return;

        }


        if (ndkDirectory == null || !ndkDirectory.isDirectory()) {
            throw new GradleException("NDK not configured.\n" + "Download the NDK from http://developer.android.com/tools/sdk/ndk/." + "Then add ndk.dir=path/to/ndk in local.properties.\n" + "(On Windows, make sure you escape backslashes, e.g. C:\\\\ndk rather than C:\\ndk)");
        }


        final Reference<Boolean> generateMakefile = new groovy.lang.Reference<boolean>(false);

        if (!inputs.isIncremental().asBoolean()) {
            getProject().getLogger().info("Unable do incremental execution: full task run");
            generateMakefile.set(true);
            FileUtils.emptyFolder(getSoFolder());
            FileUtils.emptyFolder(getObjFolder());
        } else {
            // look for added or removed files *only*

            //noinspection GroovyAssignabilityCheck
            inputs.outOfDate(new Closure(this, this) {
                public Boolean doCall(Object change) {
                    if (((InputFileDetails) change).isAdded()) {
                        return setGroovyRef(generateMakefile, true);
                    }

                }

            });

            //noinspection GroovyAssignabilityCheck
            inputs.removed(new Closure(this, this) {
                public Boolean doCall(Object change) {
                    return setGroovyRef(generateMakefile, true);
                }

            });
        }


        if (generateMakefile.get()) {
            writeMakefile(sourceFiles, makefile);
        }


        // now build
        runNdkBuild(ndkDirectory, makefile);
    }

    private void writeMakefile(@NonNull Set<File> sourceFiles, @NonNull File makefile) {
        CoreNdkOptions ndk = getNdkConfig();

        StringBuilder sb = new StringBuilder();

        sb.append("LOCAL_PATH := $(call my-dir)\n" + "include \$(CLEAR_VARS)\n\n");

        String moduleName = ndk.getModuleName() != null ? ndk.getModuleName() : getProject().getName();

        sb.append("LOCAL_MODULE := ").append(moduleName).append("\n");

        if (ndk.getcFlags() != null) {
            sb.append("LOCAL_CFLAGS := ").append(ndk.getcFlags()).append("\n");
        }


        // To support debugging from Android Studio.
        sb.append("LOCAL_LDFLAGS := -Wl,--build-id\n");

        List<String> fullLdlibs = Lists.newArrayList();
        if (ndk.getLdLibs() != null) {
            fullLdlibs.addAll(ndk.getLdLibs());
        }


        if (!fullLdlibs.isEmpty().asBoolean()) {
            sb.append("LOCAL_LDLIBS := \\\n");
            for (String lib : fullLdlibs) {
                sb.append("\t-l").append(lib).append(" \\\n");
            }

            sb.append("\n");
        }


        sb.append("LOCAL_SRC_FILES := \\\n");
        for (File sourceFile : sourceFiles) {
            sb.append("\t").append(sourceFile.getAbsolutePath()).append(" \\\n");
        }

        sb.append("\n");

        for (File sourceFolder : getSourceFolders()) {
            sb.append("LOCAL_C_INCLUDES += " + sourceFolder.getAbsolutePath() + "\n");
        }


        sb.append("\ninclude \$(BUILD_SHARED_LIBRARY)\n");

        Files.write(sb.toString(), makefile, Charsets.UTF_8);
    }

    private void runNdkBuild(@NonNull File ndkLocation, @NonNull File makefile) {
        CoreNdkOptions ndk = getNdkConfig();

        ProcessInfoBuilder builder = new ProcessInfoBuilder();

        String exe = ndkLocation.getAbsolutePath() + File.separator + "ndk-build";
        if (CURRENT_PLATFORM == PLATFORM_WINDOWS && !ndkCygwinMode) {
            exe += ".cmd";
        }

        builder.setExecutable(exe);

        builder.addArgs("NDK_PROJECT_PATH=null", "APP_BUILD_SCRIPT=" + makefile.getAbsolutePath());

        // target
        IAndroidTarget target = getBuilder().getTarget();
        if (!target.isPlatform().asBoolean()) {
            target = target.getParent();
        }

        builder.invokeMethod("addArgs", new Object[]{"APP_PLATFORM=" + target.hashString()});

        // temp out
        builder.invokeMethod("addArgs", new Object[]{"NDK_OUT=" + getObjFolder().getAbsolutePath()});

        // libs out
        builder.invokeMethod("addArgs", new Object[]{"NDK_LIBS_OUT=" + getSoFolder().getAbsolutePath()});

        // debug builds
        if (getDebuggable()) {
            builder.addArgs("NDK_DEBUG=1");
        }


        if (ndk.getStl() != null) {
            builder.invokeMethod("addArgs", new Object[]{"APP_STL=" + ndk.getStl()});
        }


        Set<String> abiFilters = ndk.getAbiFilters();
        if (abiFilters != null && !abiFilters.isEmpty()) {
            if (abiFilters.size() == 1) {
                builder.invokeMethod("addArgs", new Object[]{"APP_ABI=" + abiFilters.iterator().next()});
            } else {
                Joiner joiner = Joiner.on(",").skipNulls();
                builder.invokeMethod("addArgs", new Object[]{"APP_ABI=" + joiner.join(abiFilters.iterator())});
            }

        } else {
            builder.addArgs("APP_ABI=all");
        }


        if (ndk.getJobs() != null) {
            builder.invokeMethod("addArgs", new Object[]{"-j" + ndk.getJobs()});
        }


        ProcessOutputHandler handler = new LoggedProcessOutputHandler(getBuilder().getLogger());
        getBuilder().executeProcess(builder.createProcess(), handler).rethrowFailure().assertNormalExitValue();
    }

    private boolean isNdkOptionUnset() {
        // If none of the NDK options are set, then it is likely that NDK is not configured.
        return (getModuleName() == null && getcFlags() == null && getLdLibs() == null && getAbiFilters() == null && getStl() == null);
    }

    public List<File> getSourceFolders() {
        return sourceFolders;
    }

    public void setSourceFolders(List<File> sourceFolders) {
        this.sourceFolders = sourceFolders;
    }

    public File getGeneratedMakefile() {
        return generatedMakefile;
    }

    public void setGeneratedMakefile(File generatedMakefile) {
        this.generatedMakefile = generatedMakefile;
    }

    public boolean getDebuggable() {
        return debuggable;
    }

    public boolean isDebuggable() {
        return debuggable;
    }

    public void setDebuggable(boolean debuggable) {
        this.debuggable = debuggable;
    }

    public File getSoFolder() {
        return soFolder;
    }

    public void setSoFolder(File soFolder) {
        this.soFolder = soFolder;
    }

    public File getObjFolder() {
        return objFolder;
    }

    public void setObjFolder(File objFolder) {
        this.objFolder = objFolder;
    }

    public File getNdkDirectory() {
        return ndkDirectory;
    }

    public void setNdkDirectory(File ndkDirectory) {
        this.ndkDirectory = ndkDirectory;
    }

    public boolean getNdkCygwinMode() {
        return ndkCygwinMode;
    }

    public boolean isNdkCygwinMode() {
        return ndkCygwinMode;
    }

    public void setNdkCygwinMode(boolean ndkCygwinMode) {
        this.ndkCygwinMode = ndkCygwinMode;
    }

}
