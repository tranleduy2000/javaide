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

package com.android.build.gradle.internal.tasks;

import static com.android.utils.FileUtils.copyFile;

import com.android.annotations.NonNull;
import com.android.build.gradle.internal.test.report.ReportType;
import com.android.build.gradle.internal.test.report.TestReport;
import com.android.utils.FileUtils;
import com.google.common.collect.Lists;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.logging.ConsoleRenderer;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Task doing test report aggregation.
 */

public class AndroidReportTask extends BaseTask implements AndroidTestTask {

    private final List<AndroidTestTask> subTasks = Lists.newArrayList();

    private ReportType reportType;

    private boolean ignoreFailures;

    private boolean testFailed;

    private File reportsDir;

    private File resultsDir;


    @OutputDirectory
    public File getReportsDir() {
        return reportsDir;
    }

    public void setReportsDir(@NonNull File reportsDir) {
        this.reportsDir = reportsDir;
    }

    @Override
    @OutputDirectory
    public File getResultsDir() {
        return resultsDir;
    }

    public void setResultsDir(@NonNull File resultsDir) {
        this.resultsDir = resultsDir;
    }

    @Override
    public boolean getTestFailed() {
        return testFailed;
    }

    @Override
    public boolean getIgnoreFailures() {
        return ignoreFailures;
    }

    @Override
    public void setIgnoreFailures(boolean ignoreFailures) {
        this.ignoreFailures = ignoreFailures;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }

    public void addTask(AndroidTestTask task) {
        subTasks.add(task);
        this.dependsOn(task);
    }

    @InputFiles
    public List<File> getResultInputs() {
        List<File> list = Lists.newArrayList();

        for (AndroidTestTask task : subTasks) {
            list.add(task.getResultsDir());
        }

        return list;
    }

    /**
     * Sets that this current task will run and therefore needs to tell its children
     * class to not stop on failures.
     */
    public void setWillRun() {
        for (AndroidTestTask task : subTasks) {
            task.setIgnoreFailures(true);
        }
    }

    @TaskAction
    public void createReport() throws IOException {
        File resultsOutDir = getResultsDir();
        File reportOutDir = getReportsDir();

        // empty the folders
        FileUtils.emptyFolder(resultsOutDir);
        FileUtils.emptyFolder(reportOutDir);

        // do the copy.
        copyResults(resultsOutDir);

        // create the report.
        TestReport report = new TestReport(reportType, resultsOutDir, reportOutDir);
        report.generateReport();

        // fail if any of the tasks failed.
        for (AndroidTestTask task : subTasks) {
            if (task.getTestFailed()) {
                testFailed = true;
                String reportUrl = new ConsoleRenderer().asClickableFileUrl(
                        new File(reportOutDir, "index.html"));
                String message = "There were failing tests. See the report at: " + reportUrl;

                if (getIgnoreFailures()) {
                    getLogger().warn(message);
                } else {
                    throw new GradleException(message);
                }

                break;
            }
        }
    }

    private void copyResults(File reportOutDir) throws IOException {
        List<File> inputs = getResultInputs();

        for (File input : inputs) {
            File[] children = input.listFiles();
            if (children != null) {
                for (File child : children) {
                    copyFile(child, reportOutDir);
                }
            }
        }
    }


}
