/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.tools.lint;

import com.android.tools.lint.detector.api.Project;
import com.android.tools.lint.detector.api.Severity;
import com.android.utils.SdkUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Closer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * "Multiplexing" reporter which allows output to be split up into a separate
 * report for each separate project. It also adds an overview index.
 */
public class MultiProjectHtmlReporter extends HtmlReporter {
    private static final String INDEX_NAME = "index.html"; //$NON-NLS-1$
    private final File mDir;

    public MultiProjectHtmlReporter(LintCliClient client, File dir) throws IOException {
        super(client, new File(dir, INDEX_NAME));
        mDir = dir;
    }

    @Override
    public void write(int errorCount, int warningCount, List<Warning> allIssues) throws IOException {
        Map<Project, List<Warning>> projectToWarnings = new HashMap<Project, List<Warning>>();
        for (Warning warning : allIssues) {
            List<Warning> list = projectToWarnings.get(warning.project);
            if (list == null) {
                list = new ArrayList<Warning>();
                projectToWarnings.put(warning.project, list);
            }
            list.add(warning);
        }


        // Set of unique file names: lowercase names to avoid case conflicts in web environment
        Set<String> unique = Sets.newHashSet();
        unique.add(INDEX_NAME.toLowerCase(Locale.US));
        List<ProjectEntry> projects = Lists.newArrayList();

        for (Project project : projectToWarnings.keySet()) {
            // TODO: Can I get the project name from the Android manifest file instead?
            String projectName = project.getName();

            // Produce file names of the form Project.html, Project1.html, Project2.html, etc
            int number = 1;
            String fileName;
            while (true) {
                String numberString = number > 1 ? Integer.toString(number) : "";
                fileName = String.format("%1$s%2$s.html", projectName, numberString); //$NON-NLS-1$
                String lowercase = fileName.toLowerCase(Locale.US);
                if (!unique.contains(lowercase)) {
                    unique.add(lowercase);
                    break;
                }
                number++;
            }

            File output = new File(mDir, fileName);
            if (output.exists()) {
                boolean deleted = output.delete();
                if (!deleted) {
                    mClient.log(null, "Could not delete old file %1$s", output);
                    continue;
                }
            }
            if (!output.getParentFile().canWrite()) {
                mClient.log(null, "Cannot write output file %1$s", output);
                continue;
            }
            HtmlReporter reporter = new HtmlReporter(mClient, output);
            reporter.setBundleResources(mBundleResources);
            reporter.setSimpleFormat(mSimpleFormat);
            reporter.setUrlMap(mUrlMap);

            List<Warning> issues = projectToWarnings.get(project);
            int projectErrorCount = 0;
            int projectWarningCount = 0;
            for (Warning warning: issues) {
                if (warning.severity == Severity.ERROR || warning.severity == Severity.FATAL) {
                    projectErrorCount++;
                } else if (warning.severity == Severity.WARNING) {
                    projectWarningCount++;
                }
            }

            String prefix = project.getReferenceDir().getPath();
            String path = project.getDir().getPath();
            String relative;
            if (path.startsWith(prefix) && path.length() > prefix.length()) {
                int i = prefix.length();
                if (path.charAt(i) == File.separatorChar) {
                    i++;
                }
                relative = path.substring(i);
            } else {
                relative = projectName;
            }
            reporter.setTitle(String.format("Lint Report for %1$s", relative));
            reporter.setStripPrefix(relative);
            reporter.write(projectErrorCount, projectWarningCount, issues);

            projects.add(new ProjectEntry(fileName, projectErrorCount, projectWarningCount,
                    relative));
        }

        Closer closer = Closer.create();
        // Write overview index?
        try {
            closer.register(mWriter);
            writeOverview(errorCount, warningCount, projects);
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }

        if (!mClient.getFlags().isQuiet()
                && (mDisplayEmpty || errorCount > 0 || warningCount > 0)) {
            File index = new File(mDir, INDEX_NAME);
            String url = SdkUtils.fileToUrlString(index.getAbsoluteFile());
            System.out.println(String.format("Wrote overview index to %1$s", url));
        }
    }

    private void writeOverview(int errorCount, int warningCount, List<ProjectEntry> projects)
            throws IOException {
        mWriter.write(
                "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" + //$NON-NLS-1$
                "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +      //$NON-NLS-1$
                "<head>\n" +                                             //$NON-NLS-1$
                "<title>" + mTitle + "</title>\n");                      //$NON-NLS-1$//$NON-NLS-2$
        writeStyleSheet();
        mWriter.write(
                "</head>\n" +                                            //$NON-NLS-1$
                "<body>\n" +                                             //$NON-NLS-1$
                "<h1>" +                                                 //$NON-NLS-1$
                mTitle +
                "</h1>\n" +                                              //$NON-NLS-1$
                "<div class=\"titleSeparator\"></div>\n");               //$NON-NLS-1$


        // Sort project list in decreasing order of errors, warnings and names
        Collections.sort(projects);

        mWriter.write(String.format("Check performed at %1$s.",
                new Date().toString()));
        mWriter.write("<br/>\n");                                        //$NON-NLS-1$
        mWriter.write(String.format("%1$d errors and %2$d warnings found:\n",
                errorCount, warningCount));

        mWriter.write("<br/><br/>\n");                                   //$NON-NLS-1$

        if (errorCount == 0 && warningCount == 0) {
            mWriter.write("Congratulations!");
            return;
        }

        String errorUrl = null;
        String warningUrl = null;
        if (!mSimpleFormat) {
            errorUrl = addLocalResources(HtmlReporter.getErrorIconUrl());
            warningUrl = addLocalResources(HtmlReporter.getWarningIconUrl());
        }

        mWriter.write("<table class=\"overview\">\n");                   //$NON-NLS-1$
        mWriter.write("<tr><th>");                                       //$NON-NLS-1$
        mWriter.write("Project");
        mWriter.write("</th><th class=\"countColumn\">");                   //$NON-NLS-1$

        if (errorUrl != null) {
            mWriter.write("<img border=\"0\" align=\"top\" src=\"");      //$NON-NLS-1$
            mWriter.write(errorUrl);
            mWriter.write("\" alt=\"Error\" />\n");                          //$NON-NLS-1$
        }
        mWriter.write("Errors");
        mWriter.write("</th><th class=\"countColumn\">");                   //$NON-NLS-1$

        if (warningUrl != null) {
            mWriter.write("<img border=\"0\" align=\"top\" src=\"");      //$NON-NLS-1$
            mWriter.write(warningUrl);
            mWriter.write("\" alt=\"Warning\" />\n");                          //$NON-NLS-1$
        }
        mWriter.write("Warnings");
        mWriter.write("</th></tr>\n");                                   //$NON-NLS-1$

        for (ProjectEntry entry : projects) {
            mWriter.write("<tr><td>");                                   //$NON-NLS-1$
            mWriter.write("<a href=\"");
            appendEscapedText(entry.fileName);
            mWriter.write("\">");                                        //$NON-NLS-1$
            mWriter.write(entry.path);
            mWriter.write("</a></td><td class=\"countColumn\">");        //$NON-NLS-1$
            mWriter.write(Integer.toString(entry.errorCount));
            mWriter.write("</td><td class=\"countColumn\">");            //$NON-NLS-1$
            mWriter.write(Integer.toString(entry.warningCount));
            mWriter.write("</td></tr>\n");                               //$NON-NLS-1$
        }
        mWriter.write("</table>\n");                                     //$NON-NLS-1$

        mWriter.write("</body>\n</html>\n");                             //$NON-NLS-1$
    }

    private static class ProjectEntry implements Comparable<ProjectEntry> {
        public final int errorCount;
        public final int warningCount;
        public final String fileName;
        public final String path;


        public ProjectEntry(String fileName, int errorCount, int warningCount, String path) {
            this.fileName = fileName;
            this.errorCount = errorCount;
            this.warningCount = warningCount;
            this.path = path;
        }

        @Override
        public int compareTo(ProjectEntry other) {
            int delta = other.errorCount - errorCount;
            if (delta != 0) {
                return delta;
            }

            delta = other.warningCount - warningCount;
            if (delta != 0) {
                return delta;
            }

            return path.compareTo(other.path);
        }
    }
}
