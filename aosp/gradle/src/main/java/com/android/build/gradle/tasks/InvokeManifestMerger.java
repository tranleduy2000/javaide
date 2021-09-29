package com.android.build.gradle.tasks;

import com.android.build.gradle.internal.LoggerWrapper;
import com.android.build.gradle.internal.tasks.DefaultAndroidTask;
import com.android.manifmerger.ManifestMerger2;
import com.android.manifmerger.MergingReport;
import com.android.utils.ILogger;
import com.google.common.base.Supplier;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.ParallelizableTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Simple task to invoke the new Manifest Merger without any injection, features, system properties
 * or overlay manifests
 */
@ParallelizableTask
public class InvokeManifestMerger extends DefaultAndroidTask implements Supplier<File> {
    @TaskAction
    protected void doFullTaskAction() throws ManifestMerger2.MergeFailureException, IOException {
        ILogger iLogger = new LoggerWrapper(getLogger());
        ManifestMerger2.Invoker mergerInvoker = ManifestMerger2.newMerger(getMainManifestFile(), iLogger, ManifestMerger2.MergeType.APPLICATION);
        mergerInvoker.addLibraryManifests(secondaryManifestFiles.toArray(new File[secondaryManifestFiles.size()]));
        MergingReport mergingReport = mergerInvoker.merge();
        if (mergingReport.getResult().isError()) {
            getLogger().error(mergingReport.getReportString());
            mergingReport.log(iLogger);
            throw new GradleException(mergingReport.getReportString());
        }

        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(getOutputFile());
            fileWriter.append(mergingReport.getMergedDocument().get().prettyPrint());
        } finally {
            if (fileWriter != null) {
                fileWriter.close();
            }

        }

    }

    @Override
    public File get() {
        return getOutputFile();
    }

    public File getMainManifestFile() {
        return mainManifestFile;
    }

    public void setMainManifestFile(File mainManifestFile) {
        this.mainManifestFile = mainManifestFile;
    }

    public List<File> getSecondaryManifestFiles() {
        return secondaryManifestFiles;
    }

    public void setSecondaryManifestFiles(List<File> secondaryManifestFiles) {
        this.secondaryManifestFiles = secondaryManifestFiles;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    @InputFile
    private File mainManifestFile;
    @InputFiles
    private List<File> secondaryManifestFiles;
    @OutputFile
    private File outputFile;
}
