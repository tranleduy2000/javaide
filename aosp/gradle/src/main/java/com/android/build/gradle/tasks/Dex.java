package com.android.build.gradle.tasks;

import com.android.SdkConstants;
import com.android.annotations.Nullable;
import com.android.build.gradle.internal.PostCompilationData;
import com.android.build.gradle.internal.core.GradleVariantConfiguration;
import com.android.build.gradle.internal.dsl.DexOptions;
import com.android.build.gradle.internal.scope.ConventionMappingHelper;
import com.android.build.gradle.internal.scope.TaskConfigAction;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.build.gradle.internal.tasks.BaseTask;
import com.android.build.gradle.internal.variant.ApkVariantData;
import com.android.ide.common.process.LoggedProcessOutputHandler;
import com.android.utils.FileUtils;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.incremental.IncrementalTaskInputs;
import org.gradle.api.tasks.incremental.InputFileDetails;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

public class Dex extends BaseTask {
    @OutputDirectory
    private File outputFolder;
    @Input
    @Optional
    private List<String> additionalParameters;
    private boolean enableIncremental = true;
    @InputFiles
    @Optional
    private Collection<File> inputFiles;
    @InputDirectory
    @Optional
    private File inputDir;
    @InputFiles
    private Collection<File> libraries;
    @Nested
    private DexOptions dexOptions;
    @Input
    private boolean multiDexEnabled = false;
    @Input
    private boolean optimize = true;
    @InputFile
    @Optional
    private File mainDexListFile;
    private File tmpFolder;

    @Input
    public String getBuildToolsVersion() {
        return getBuildTools().getRevision();
    }

    /**
     * Actual entry point for the action.
     * Calls out to the doTaskAction as needed.
     */
    @TaskAction
    public void taskAction(IncrementalTaskInputs inputs) {
        Collection<File> _inputFiles = getInputFiles();
        File _inputDir = getInputDir();
        if (_inputFiles == null && _inputDir == null) {
            throw new RuntimeException("Dex task \'" + getName() + ": inputDir and inputFiles cannot both be null");
        }


        if (!dexOptions.getIncremental() || !enableIncremental) {
            doTaskAction(_inputFiles, _inputDir, false);
            return;

        }


        if (!inputs.isIncremental().asBoolean()) {
            getProject().getLogger().info("Unable to do incremental execution: full task run.");
            doTaskAction(_inputFiles, _inputDir, false);
            return;

        }


        final AtomicBoolean forceFullRun = new AtomicBoolean();

        //noinspection GroovyAssignabilityCheck
        inputs.outOfDate(new Closure(this, this) {
            public void doCall(final Object change) {
                // force full dx run if existing jar file is modified
                // New jar files are fine.
                if (((InputFileDetails) change).isModified() && ((InputFileDetails) change).getFile().getPath().endsWith(SdkConstants.DOT_JAR)) {
                    getProject().getLogger().info("Force full dx run: Found updated " + String.valueOf(((InputFileDetails) change).getFile()));
                    forceFullRun.set(true);
                }

            }

        });

        //noinspection GroovyAssignabilityCheck
        inputs.removed(new Closure(this, this) {
            public void doCall(final Object change) {
                // force full dx run if existing jar file is removed
                if (((InputFileDetails) change).getFile().getPath().endsWith(SdkConstants.DOT_JAR)) {
                    getProject().getLogger().info("Force full dx run: Found removed " + String.valueOf(((InputFileDetails) change).getFile()));
                    forceFullRun.set(true);
                }

            }

        });

        doTaskAction(_inputFiles, _inputDir, !forceFullRun.get());
    }

    private void doTaskAction(@Nullable Collection<File> inputFiles, @Nullable File inputDir, boolean incremental) {
        File outFolder = getOutputFolder();
        if (!incremental.asBoolean()) {
            FileUtils.emptyFolder(outFolder);
        }


        File tmpFolder = getTmpFolder();
        tmpFolder.mkdirs();

        // if some of our .jar input files exist, just reset the inputDir to null
        for (File inputFile : inputFiles) {
            if (inputFile.exists()) {
                inputDir = null;
            }

        }

        if (inputDir != null) {
            inputFiles = getProject().files(inputDir).getFiles();
        }


        getBuilder().convertByteCode(inputFiles, getLibraries(), outFolder, getMultiDexEnabled(), getMainDexListFile(), getDexOptions(), getAdditionalParameters(), tmpFolder, incremental, getOptimize(), new LoggedProcessOutputHandler(getILogger()));
    }

    public File getOutputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(File outputFolder) {
        this.outputFolder = outputFolder;
    }

    public List<String> getAdditionalParameters() {
        return additionalParameters;
    }

    public void setAdditionalParameters(List<String> additionalParameters) {
        this.additionalParameters = additionalParameters;
    }

    public boolean getEnableIncremental() {
        return enableIncremental;
    }

    public boolean isEnableIncremental() {
        return enableIncremental;
    }

    public void setEnableIncremental(boolean enableIncremental) {
        this.enableIncremental = enableIncremental;
    }

    public Collection<File> getInputFiles() {
        return inputFiles;
    }

    public void setInputFiles(Collection<File> inputFiles) {
        this.inputFiles = inputFiles;
    }

    public File getInputDir() {
        return inputDir;
    }

    public void setInputDir(File inputDir) {
        this.inputDir = inputDir;
    }

    public Collection<File> getLibraries() {
        return libraries;
    }

    public void setLibraries(Collection<File> libraries) {
        this.libraries = libraries;
    }

    public DexOptions getDexOptions() {
        return dexOptions;
    }

    public void setDexOptions(DexOptions dexOptions) {
        this.dexOptions = dexOptions;
    }

    public boolean getMultiDexEnabled() {
        return multiDexEnabled;
    }

    public boolean isMultiDexEnabled() {
        return multiDexEnabled;
    }

    public void setMultiDexEnabled(boolean multiDexEnabled) {
        this.multiDexEnabled = multiDexEnabled;
    }

    public boolean getOptimize() {
        return optimize;
    }

    public boolean isOptimize() {
        return optimize;
    }

    public void setOptimize(boolean optimize) {
        this.optimize = optimize;
    }

    public File getMainDexListFile() {
        return mainDexListFile;
    }

    public void setMainDexListFile(File mainDexListFile) {
        this.mainDexListFile = mainDexListFile;
    }

    public File getTmpFolder() {
        return tmpFolder;
    }

    public void setTmpFolder(File tmpFolder) {
        this.tmpFolder = tmpFolder;
    }

    public static class ConfigAction extends GroovyObjectSupport implements TaskConfigAction<Dex> {
        private final VariantScope scope;
        private final PostCompilationData pcData;

        public ConfigAction(VariantScope scope, PostCompilationData pcData) {
            this.scope = scope;
            this.pcData = pcData;
        }

        @Override
        public String getName() {
            return scope.getTaskName("dex");
        }

        @Override
        public Class<Dex> getType() {
            return ((Class<Dex>) (Dex.class));
        }

        @Override
        public void execute(Dex dexTask) {
            ApkVariantData variantData = (ApkVariantData) scope.getVariantData();
            final GradleVariantConfiguration config = variantData.getVariantConfiguration();

            boolean isMultiDexEnabled = config.isMultiDexEnabled();
            boolean isLegacyMultiDexMode = config.isLegacyMultiDexMode();

            variantData.dexTask = dexTask;
            dexTask.setAndroidBuilder(scope.getGlobalScope().getAndroidBuilder());
            dexTask.setVariantName(config.getFullName());
            ConventionMappingHelper.map(dexTask, "outputFolder", new Callable<File>() {
                @Override
                public File call() throws Exception {
                    return scope.getDexOutputFolder();
                }

            });
            dexTask.setTmpFolder(new File(String.valueOf(scope.getGlobalScope().getBuildDir()) + "/" + FD_INTERMEDIATES + "/tmp/dex/" + config.getDirName()));
            dexTask.setDexOptions(scope.getGlobalScope().getExtension().getDexOptions());
            dexTask.setMultiDexEnabled(isMultiDexEnabled);
            // dx doesn't work with receving --no-optimize in debug so we disable it for now.
            dexTask.setOptimize(true);//!variantData.variantConfiguration.buildType.debuggable

            // inputs
            if (pcData.getInputDirCallable() != null) {
                ConventionMappingHelper.map(dexTask, "inputDir", pcData.getInputDirCallable());
            }

            ConventionMappingHelper.map(dexTask, "inputFiles", pcData.getInputFilesCallable());
            ConventionMappingHelper.map(dexTask, "libraries", pcData.getInputLibrariesCallable());

            if (isMultiDexEnabled && isLegacyMultiDexMode) {
                // configure the dex task to receive the generated class list.
                ConventionMappingHelper.map(dexTask, "mainDexListFile", new Callable<File>() {
                    @Override
                    public File call() throws Exception {
                        return scope.getMainDexListFile();
                    }

                });
            }

        }
    }
}
