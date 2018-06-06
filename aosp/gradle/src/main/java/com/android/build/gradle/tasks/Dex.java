package com.android.build.gradle.tasks;

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
import com.android.ide.common.process.ProcessException;
import com.android.sdklib.repository.FullRevision;
import com.android.utils.FileUtils;

import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import static com.android.builder.model.AndroidProject.FD_INTERMEDIATES;

public class Dex extends BaseTask {

    private File outputFolder;
    private List<String> additionalParameters;
    private Collection<File> inputFiles;
    private File inputDir;
    private Collection<File> libraries;

    private DexOptions dexOptions;
    private boolean multiDexEnabled = false;
    private boolean optimize = true;
    private File mainDexListFile;
    private File tmpFolder;

    public FullRevision getBuildToolsVersion() {
        return getBuildTools().getRevision();
    }

    /**
     * Actual entry point for the action.
     * Calls out to the doTaskAction as needed.
     */
    @TaskAction
    public void taskAction() throws IOException, InterruptedException, ProcessException {
        Collection<File> _inputFiles = getInputFiles();
        File _inputDir = getInputDir();
        if (_inputFiles == null && _inputDir == null) {
            throw new RuntimeException("Dex task \'" + getName() + ": inputDir and inputFiles cannot both be null");
        }

        doTaskAction(_inputFiles, _inputDir);
    }

    private void doTaskAction(@Nullable Collection<File> inputFiles, @Nullable File inputDir) throws IOException, ProcessException, InterruptedException {
        File outFolder = getOutputFolder();
        FileUtils.emptyFolder(outFolder);


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


        getBuilder().convertByteCode(inputFiles, getLibraries(), outFolder, getMultiDexEnabled(),
                getMainDexListFile(), getDexOptions(), getAdditionalParameters(), tmpFolder,
                false, getOptimize(), new LoggedProcessOutputHandler(getILogger()));
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

    public static class ConfigAction implements TaskConfigAction<Dex> {
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
            return Dex.class;
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
                public File call() {
                    return scope.getDexOutputFolder();
                }

            });
            dexTask.setTmpFolder(new File(String.valueOf(scope.getGlobalScope().getBuildDir())
                    + "/" + FD_INTERMEDIATES + "/tmp/dex/" + config.getDirName()));
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
                    public File call() {
                        return scope.getMainDexListFile();
                    }

                });
            }

        }
    }
}
