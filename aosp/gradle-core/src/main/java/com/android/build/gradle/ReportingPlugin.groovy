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

package com.android.build.gradle;
import com.android.build.gradle.internal.tasks.AndroidReportTask;
import com.android.build.gradle.internal.tasks.DeviceProviderInstrumentTestTask;
import com.android.build.gradle.internal.dsl.TestOptions;
import com.android.build.gradle.internal.test.report.ReportType;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.tasks.TaskCollection;

import static com.android.builder.core.BuilderConstants.FD_ANDROID_RESULTS;
import static com.android.builder.core.BuilderConstants.FD_ANDROID_TESTS;
import static com.android.builder.core.BuilderConstants.FD_REPORTS;
/**
 * Gradle plugin class for 'reporting' projects.
 *
 * This is mostly used to aggregate reports from subprojects.
 *
 */
class ReportingPlugin implements org.gradle.api.Plugin<Project> {

    private TestOptions extension

    @Override
    void apply(Project project) {
        // make sure this project depends on the evaluation of all sub projects so that
        // it's evaluated last.
        project.evaluationDependsOnChildren()

        extension = project.extensions.create('android', TestOptions)

        AndroidReportTask mergeReportsTask = project.tasks.create("mergeAndroidReports",
                AndroidReportTask)
        mergeReportsTask.group = JavaBasePlugin.VERIFICATION_GROUP
        mergeReportsTask.description = "Merges all the Android test reports from the sub projects."
        mergeReportsTask.reportType = ReportType.MULTI_PROJECT
        mergeReportsTask.setVariantName("")

        mergeReportsTask.conventionMapping.resultsDir = {
            String location = extension.resultsDir != null ?
                extension.resultsDir : "$project.buildDir/$FD_ANDROID_RESULTS"

            project.file(location)
        }
        mergeReportsTask.conventionMapping.reportsDir = {
            String location = extension.reportDir != null ?
                extension.reportDir : "$project.buildDir/$FD_REPORTS/$FD_ANDROID_TESTS"

            project.file(location)
        }

        // gather the subprojects
        project.afterEvaluate {
            project.subprojects.each { p ->
                TaskCollection<AndroidReportTask> tasks = p.tasks.withType(AndroidReportTask)
                for (AndroidReportTask task : tasks) {
                    mergeReportsTask.addTask(task)
                }
                TaskCollection<DeviceProviderInstrumentTestTask> tasks2 = p.tasks.withType(DeviceProviderInstrumentTestTask)
                for (DeviceProviderInstrumentTestTask task : tasks2) {
                    mergeReportsTask.addTask(task)
                }
            }
        }

        // If gradle is launched with --continue, we want to run all tests and generate an
        // aggregate report (to help with the fact that we may have several build variants).
        // To do that, the "mergeAndroidReports" task (which does the aggregation) must always
        // run even if one of its dependent task (all the testFlavor tasks) fails, so we make
        // them ignore their error.
        // We cannot do that always: in case the test task is not going to run, we do want the
        // individual testFlavor tasks to fail.
        if (mergeReportsTask != null && project.gradle.startParameter.continueOnFailure) {
            project.gradle.taskGraph.whenReady { taskGraph ->
                if (taskGraph.hasTask(mergeReportsTask)) {
                    mergeReportsTask.setWillRun()
                }
            }
        }
    }
}
