package com.android.build.gradle.tasks;

import com.android.annotations.NonNull;
import com.android.build.gradle.internal.annotations.ApkFile;
import com.android.build.gradle.internal.scope.TaskConfigAction;
import com.android.build.gradle.internal.scope.VariantOutputScope;
import com.android.build.gradle.internal.tasks.FileSupplier;
import com.android.build.gradle.internal.variant.ApkVariantOutputData;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.ParallelizableTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecSpec;

import java.io.File;

import static com.android.sdklib.BuildToolInfo.PathId.ZIP_ALIGN;

@ParallelizableTask
public class ZipAlign extends DefaultTask implements FileSupplier {

    // ----- PUBLIC TASK API -----

    private File outputFile;
    @ApkFile
    private File inputFile;
    @ApkFile
    private File zipAlignExe;

    @OutputFile
    public File getOutputFile() {
        return outputFile;
    }

    // ----- PRIVATE TASK API -----

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    @InputFile
    public File getInputFile() {
        return inputFile;
    }

    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }

    @InputFile
    public File getZipAlignExe() {
        return zipAlignExe;
    }

    public void setZipAlignExe(File zipAlignExe) {
        this.zipAlignExe = zipAlignExe;
    }

    @TaskAction
    public void zipAlign() {
        getProject().exec(new Action<ExecSpec>() {
            @Override
            public void execute(ExecSpec execSpec) {
                execSpec.executable(getZipAlignExe());
                execSpec.args("-f", "4");
                execSpec.args(getInputFile());
                execSpec.args(getOutputFile());
            }
        });
    }

    // ----- FileSupplierTask -----

    @Override
    public File get() {
        return getOutputFile();
    }

    @NonNull
    @Override
    public Task getTask() {
        return this;
    }

    // ----- ConfigAction -----

    public static class ConfigAction implements TaskConfigAction<ZipAlign> {

        private final VariantOutputScope scope;

        public ConfigAction(VariantOutputScope scope) {
            this.scope = scope;
        }

        @Override
        public String getName() {
            return scope.getTaskName("zipalign");
        }

        @Override
        public Class<ZipAlign> getType() {
            return ZipAlign.class;
        }

        @Override
        public void execute(@android.support.annotation.NonNull ZipAlign zipAlign) {
            ((ApkVariantOutputData) scope.getVariantOutputData()).zipAlignTask = zipAlign;
            zipAlign.setInputFile(scope.getPackageApk());
            zipAlign.setOutputFile(scope.getGlobalScope().getProject().file(
                    scope.getGlobalScope().getApkLocation() + "/" +
                            scope.getGlobalScope().getProjectBaseName() + "-" +
                            scope.getVariantOutputData().getBaseName() + ".apk"));

            File zipAlignExe = null;
            String path = scope.getGlobalScope().getAndroidBuilder().getTargetInfo()
                    .getBuildTools().getPath(ZIP_ALIGN);
            if (path != null) {
                zipAlignExe = new File(path);
            }
            zipAlign.setZipAlignExe(zipAlignExe);
        }
    }

}
