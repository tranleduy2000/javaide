/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.build.gradle.internal.coverage
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
/**
 * Simple Jacoco report task that calls the Ant version.
 */
public class JacocoReportTask extends DefaultTask {
    @InputFile
    File coverageFile

    @OutputDirectory
    File reportDir

    @InputDirectory
    File classDir

    @InputFiles
    List<File> sourceDir

    @Input
    String reportName

    @InputFiles
    FileCollection jacocoClasspath

    @TaskAction
    void report() {
        File reportOutDir = getReportDir()
        reportOutDir.deleteDir()
        reportOutDir.mkdirs()

        getAnt().taskdef(name: 'reportWithJacoco',
                         classname: 'org.jacoco.ant.ReportTask',
                         classpath: getJacocoClasspath().asPath)
        getAnt().reportWithJacoco {
            executiondata {
                file(file: getCoverageFile())
            }
            structure(name: getReportName()) {
                sourcefiles {
                    for (File source : getSourceDir()) {
                        fileset(dir: source)
                    }
                }
                classfiles {
                    fileset(
                            dir: getClassDir(),
                            excludes: "**/R.class,**/R\$*.class,**/Manifest.class,**/Manifest\$*.class,**/BuildConfig.class")
                }
            }

            html(destdir: reportOutDir)
            xml(destfile: new File(reportOutDir, "report.xml"))
        }
    }
}
