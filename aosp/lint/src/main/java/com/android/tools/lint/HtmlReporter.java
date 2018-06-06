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

import static com.android.SdkConstants.DOT_JPG;
import static com.android.SdkConstants.DOT_PNG;
import static com.android.tools.lint.detector.api.LintUtils.endsWith;
import static com.android.tools.lint.detector.api.TextFormat.HTML;
import static com.android.tools.lint.detector.api.TextFormat.RAW;

import com.android.tools.lint.checks.BuiltinIssueRegistry;
import com.android.tools.lint.client.api.Configuration;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.Position;
import com.android.tools.lint.detector.api.Project;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.TextFormat;
import com.android.utils.SdkUtils;
import com.google.common.annotations.Beta;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Files;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A reporter which emits lint results into an HTML report.
 * <p>
 * <b>NOTE: This is not a public or final API; if you rely on this be prepared
 * to adjust your code for the next tools release.</b>
 */
@Beta
public class HtmlReporter extends Reporter {
    private static final boolean USE_HOLO_STYLE = true;
    @SuppressWarnings("ConstantConditions")
    private static final String CSS = USE_HOLO_STYLE
            ? "hololike.css" : "default.css"; //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Maximum number of warnings allowed for a single issue type before we
     * split up and hide all but the first {@link #SHOWN_COUNT} items.
     */
    private static final int SPLIT_LIMIT = 8;
    /**
     * When a warning has at least {@link #SPLIT_LIMIT} items, then we show the
     * following number of items before the "Show more" button/link.
     */
    private static final int SHOWN_COUNT = SPLIT_LIMIT - 3;

    protected final Writer mWriter;
    private String mStripPrefix;
    private String mFixUrl;

    /**
     * Creates a new {@link HtmlReporter}
     *
     * @param client the associated client
     * @param output the output file
     * @throws IOException if an error occurs
     */
    public HtmlReporter(LintCliClient client, File output) throws IOException {
        super(client, output);
        mWriter = new BufferedWriter(Files.newWriter(output, Charsets.UTF_8));
    }

    @Override
    public void write(int errorCount, int warningCount, List<Warning> issues) throws IOException {
        Map<Issue, String> missing = computeMissingIssues(issues);

        mWriter.write(
                "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" + //$NON-NLS-1$
                "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +      //$NON-NLS-1$
                "<head>\n" +                                             //$NON-NLS-1$
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />" + //$NON-NLS-1$
                "<title>" + mTitle + "</title>\n");                      //$NON-NLS-1$//$NON-NLS-2$

        writeStyleSheet();

        if (!mSimpleFormat) {
            // JavaScript for collapsing/expanding long lists
            mWriter.write(
                "<script language=\"javascript\" type=\"text/javascript\"> \n" + //$NON-NLS-1$
                "<!--\n" +                                               //$NON-NLS-1$
                "function reveal(id) {\n" +                              //$NON-NLS-1$
                "if (document.getElementById) {\n" +                     //$NON-NLS-1$
                "document.getElementById(id).style.display = 'block';\n" +       //$NON-NLS-1$
                "document.getElementById(id+'Link').style.display = 'none';\n" + //$NON-NLS-1$
                "}\n" +                                                  //$NON-NLS-1$
                "}\n" +                                                  //$NON-NLS-1$
                "//--> \n" +                                             //$NON-NLS-1$
                "</script>\n");                                          //$NON-NLS-1$
        }

        mWriter.write(
                "</head>\n" +                                            //$NON-NLS-1$
                "<body>\n" +                                             //$NON-NLS-1$
                "<h1>" +                                                 //$NON-NLS-1$
                mTitle +
                "</h1>\n" +                                              //$NON-NLS-1$
                "<div class=\"titleSeparator\"></div>\n");               //$NON-NLS-1$

        mWriter.write(String.format("Check performed at %1$s.",
                new Date().toString()));
        mWriter.write("<br/>\n");                                        //$NON-NLS-1$
        mWriter.write(String.format("%1$d errors and %2$d warnings found:",
                errorCount, warningCount));
        mWriter.write("<br/><br/>\n");                                   //$NON-NLS-1$

        Issue previousIssue = null;
        if (!issues.isEmpty()) {
            List<List<Warning>> related = new ArrayList<List<Warning>>();
            List<Warning> currentList = null;
            for (Warning warning : issues) {
                if (warning.issue != previousIssue) {
                    previousIssue = warning.issue;
                    currentList = new ArrayList<Warning>();
                    related.add(currentList);
                }
                assert currentList != null;
                currentList.add(warning);
            }

            writeOverview(related, missing.size());

            Category previousCategory = null;
            for (List<Warning> warnings : related) {
                Warning first = warnings.get(0);
                Issue issue = first.issue;

                if (issue.getCategory() != previousCategory) {
                    previousCategory = issue.getCategory();
                    mWriter.write("\n<a name=\"");                       //$NON-NLS-1$
                    mWriter.write(issue.getCategory().getFullName());
                    mWriter.write("\"></a>\n");                          //$NON-NLS-1$
                    mWriter.write("<div class=\"category\"><a href=\"#\" title=\"Return to top\">");           //$NON-NLS-1$
                    mWriter.write(issue.getCategory().getFullName());
                    mWriter.write("</a><div class=\"categorySeparator\"></div>\n");//$NON-NLS-1$
                    mWriter.write("</div>\n");                           //$NON-NLS-1$
                }

                mWriter.write("<a name=\"" + issue.getId() + "\"></a>\n"); //$NON-NLS-1$ //$NON-NLS-2$
                mWriter.write("<div class=\"issue\">\n");                //$NON-NLS-1$

                // Explain this issue
                mWriter.write("<div class=\"id\"><a href=\"#\" title=\"Return to top\">");                     //$NON-NLS-1$
                mWriter.write(issue.getId());
                mWriter.write(": ");                                     //$NON-NLS-1$
                mWriter.write(issue.getBriefDescription(HTML));
                mWriter.write("</a><div class=\"issueSeparator\"></div>\n"); //$NON-NLS-1$
                mWriter.write("</div>\n");                               //$NON-NLS-1$

                mWriter.write("<div class=\"warningslist\">\n");         //$NON-NLS-1$
                boolean partialHide = !mSimpleFormat && warnings.size() > SPLIT_LIMIT;

                int count = 0;
                for (Warning warning : warnings) {
                    if (partialHide && count == SHOWN_COUNT) {
                        String id = warning.issue.getId() + "Div";       //$NON-NLS-1$
                        mWriter.write("<button id=\"");                  //$NON-NLS-1$
                        mWriter.write(id);
                        mWriter.write("Link\" onclick=\"reveal('");      //$NON-NLS-1$
                        mWriter.write(id);
                        mWriter.write("');\" />");                       //$NON-NLS-1$
                        mWriter.write(String.format("+ %1$d More Occurrences...",
                                warnings.size() - SHOWN_COUNT));
                        mWriter.write("</button>\n");                    //$NON-NLS-1$
                        mWriter.write("<div id=\"");                     //$NON-NLS-1$
                        mWriter.write(id);
                        mWriter.write("\" style=\"display: none\">\n");  //$NON-NLS-1$
                    }
                    count++;
                    String url = null;
                    if (warning.path != null) {
                        url = writeLocation(warning.file, warning.path, warning.line);
                        mWriter.write(':');
                        mWriter.write(' ');
                    }

                    // Is the URL for a single image? If so, place it here near the top
                    // of the error floating on the right. If there are multiple images,
                    // they will instead be placed in a horizontal box below the error
                    boolean addedImage = false;
                    if (url != null && warning.location != null
                            && warning.location.getSecondary() == null) {
                        addedImage = addImage(url, warning.location);
                    }
                    mWriter.write("<span class=\"message\">");           //$NON-NLS-1$
                    mWriter.append(RAW.convertTo(warning.message, HTML));
                    mWriter.write("</span>");                            //$NON-NLS-1$
                    if (addedImage) {
                        mWriter.write("<br clear=\"right\"/>");          //$NON-NLS-1$
                    } else {
                        mWriter.write("<br />");                         //$NON-NLS-1$
                    }

                    // Insert surrounding code block window
                    if (warning.line >= 0 && warning.fileContents != null) {
                        mWriter.write("<pre class=\"errorlines\">\n");   //$NON-NLS-1$
                        appendCodeBlock(warning.fileContents, warning.line, warning.offset);
                        mWriter.write("\n</pre>");                       //$NON-NLS-1$
                    }
                    mWriter.write('\n');
                    if (warning.location != null && warning.location.getSecondary() != null) {
                        mWriter.write("<ul>");
                        Location l = warning.location.getSecondary();
                        int otherLocations = 0;
                        while (l != null) {
                            String message = l.getMessage();
                            if (message != null && !message.isEmpty()) {
                                Position start = l.getStart();
                                int line = start != null ? start.getLine() : -1;
                                String path = mClient.getDisplayPath(warning.project, l.getFile());
                                writeLocation(l.getFile(), path, line);
                                mWriter.write(':');
                                mWriter.write(' ');
                                mWriter.write("<span class=\"message\">");           //$NON-NLS-1$
                                mWriter.append(RAW.convertTo(message, HTML));
                                mWriter.write("</span>");                            //$NON-NLS-1$
                                mWriter.write("<br />");                         //$NON-NLS-1$

                                String name = l.getFile().getName();
                                if (!(endsWith(name, DOT_PNG) || endsWith(name, DOT_JPG))) {
                                    String s = mClient.readFile(l.getFile());
                                    if (s != null && !s.isEmpty()) {
                                        mWriter.write("<pre class=\"errorlines\">\n");   //$NON-NLS-1$
                                        int offset = start != null ? start.getOffset() : -1;
                                        appendCodeBlock(s, line, offset);
                                        mWriter.write("\n</pre>");                       //$NON-NLS-1$
                                    }
                                }
                            } else {
                                otherLocations++;
                            }

                            l = l.getSecondary();
                        }
                        mWriter.write("</ul>");
                        if (otherLocations > 0) {
                            String id = "Location" + count + "Div";          //$NON-NLS-1$
                            mWriter.write("<button id=\"");                  //$NON-NLS-1$
                            mWriter.write(id);
                            mWriter.write("Link\" onclick=\"reveal('");      //$NON-NLS-1$
                            mWriter.write(id);
                            mWriter.write("');\" />"); //$NON-NLS-1$
                            mWriter.write(String.format("+ %1$d Additional Locations...",
                                    otherLocations));
                            mWriter.write("</button>\n");                    //$NON-NLS-1$
                            mWriter.write("<div id=\"");                     //$NON-NLS-1$
                            mWriter.write(id);
                            mWriter.write("\" style=\"display: none\">\n");  //$NON-NLS-1$

                            mWriter.write("Additional locations: ");
                            mWriter.write("<ul>\n"); //$NON-NLS-1$
                            l = warning.location.getSecondary();
                            while (l != null) {
                                Position start = l.getStart();
                                int line = start != null ? start.getLine() : -1;
                                String path = mClient.getDisplayPath(warning.project, l.getFile());
                                mWriter.write("<li> "); //$NON-NLS-1$
                                writeLocation(l.getFile(), path, line);
                                mWriter.write("\n");  //$NON-NLS-1$
                                l = l.getSecondary();
                            }
                            mWriter.write("</ul>\n"); //$NON-NLS-1$

                            mWriter.write("</div><br/><br/>\n"); //$NON-NLS-1$
                        }
                    }

                    // Place a block of images?
                    if (!addedImage && url != null && warning.location != null
                            && warning.location.getSecondary() != null) {
                        addImage(url, warning.location);
                    }

                    if (warning.isVariantSpecific()) {
                        mWriter.write("\n");
                        mWriter.write("Applies to variants: ");
                        mWriter.write(Joiner.on(", ").join(warning.getIncludedVariantNames()));
                        mWriter.write("<br/>\n");
                        mWriter.write("Does <b>not</b> apply to variants: ");
                        mWriter.write(Joiner.on(", ").join(warning.getExcludedVariantNames()));
                        mWriter.write("<br/>\n");
                    }
                }
                if (partialHide) { // Close up the extra div
                    mWriter.write("</div>\n");                           //$NON-NLS-1$
                }

                mWriter.write("</div>\n");                               //$NON-NLS-1$
                writeIssueMetadata(issue, first.severity, null);

                mWriter.write("</div>\n");                               //$NON-NLS-1$
            }

            if (!mClient.isCheckingSpecificIssues()) {
                writeMissingIssues(missing);
            }

            writeSuppressInfo();
        } else {
            mWriter.write("Congratulations!");
        }
        mWriter.write("\n</body>\n</html>");                             //$NON-NLS-1$
        mWriter.close();

        if (!mClient.getFlags().isQuiet()
                && (mDisplayEmpty || errorCount > 0 || warningCount > 0)) {
            String url = SdkUtils.fileToUrlString(mOutput.getAbsoluteFile());
            System.out.println(String.format("Wrote HTML report to %1$s", url));
        }
    }

    private void writeIssueMetadata(Issue issue, Severity severity, String disabledBy)
            throws IOException {
        mWriter.write("<div class=\"metadata\">");               //$NON-NLS-1$

        if (mClient.getRegistry() instanceof BuiltinIssueRegistry) {
            boolean adtHasFix = QuickfixHandler.ADT.hasAutoFix(issue);
            boolean studioHasFix = QuickfixHandler.STUDIO.hasAutoFix(issue);
            if (adtHasFix || studioHasFix) {
                String adt = "Eclipse/ADT";
                String studio = "Android Studio/IntelliJ";
                String tools = adtHasFix && studioHasFix
                        ? (adt + " & " + studio) : studioHasFix ? studio : adt;
                mWriter.write("Note: This issue has an associated quickfix operation in " + tools);
                if (mFixUrl != null) {
                    mWriter.write("&nbsp;<img alt=\"Fix\" border=\"0\" align=\"top\" src=\""); //$NON-NLS-1$
                    mWriter.write(mFixUrl);
                    mWriter.write("\" />\n");                            //$NON-NLS-1$
                }

                mWriter.write("<br>\n");
            }
        }

        if (disabledBy != null) {
            mWriter.write(String.format("Disabled By: %1$s<br/>\n", disabledBy));
        }

        mWriter.write("Priority: ");
        mWriter.write(String.format("%1$d / 10", issue.getPriority()));
        mWriter.write("<br/>\n");                                //$NON-NLS-1$
        mWriter.write("Category: ");
        mWriter.write(issue.getCategory().getFullName());
        mWriter.write("</div>\n");                               //$NON-NLS-1$

        mWriter.write("Severity: ");
        if (severity == Severity.ERROR || severity == Severity.FATAL) {
            mWriter.write("<span class=\"error\">");             //$NON-NLS-1$
        } else if (severity == Severity.WARNING) {
            mWriter.write("<span class=\"warning\">");           //$NON-NLS-1$
        } else {
            mWriter.write("<span>");                             //$NON-NLS-1$
        }
        appendEscapedText(severity.getDescription());
        mWriter.write("</span>");                                //$NON-NLS-1$

        mWriter.write("<div class=\"summary\">\n");              //$NON-NLS-1$
        mWriter.write("Explanation: ");
        String description = issue.getBriefDescription(HTML);
        mWriter.write(description);
        if (!description.isEmpty()
                && Character.isLetter(description.charAt(description.length() - 1))) {
            mWriter.write('.');
        }
        mWriter.write("</div>\n");                               //$NON-NLS-1$
        mWriter.write("<div class=\"explanation\">\n");          //$NON-NLS-1$
        String explanationHtml = issue.getExplanation(HTML);
        mWriter.write(explanationHtml);
        mWriter.write("\n</div>\n");                             //$NON-NLS-1$;
        List<String> moreInfo = issue.getMoreInfo();
        mWriter.write("<br/>");                                  //$NON-NLS-1$
        mWriter.write("<div class=\"moreinfo\">");               //$NON-NLS-1$
        mWriter.write("More info: ");
        int count = moreInfo.size();
        if (count > 1) {
            mWriter.write("<ul>");                               //$NON-NLS-1$
        }
        for (String uri : moreInfo) {
            if (count > 1) {
                mWriter.write("<li>");                           //$NON-NLS-1$
            }
            mWriter.write("<a href=\"");                         //$NON-NLS-1$
            mWriter.write(uri);
            mWriter.write("\">"    );                            //$NON-NLS-1$
            mWriter.write(uri);
            mWriter.write("</a>\n");                             //$NON-NLS-1$
        }
        if (count > 1) {
            mWriter.write("</ul>");                              //$NON-NLS-1$
        }
        mWriter.write("</div>");                                 //$NON-NLS-1$

        mWriter.write("<br/>");                                  //$NON-NLS-1$
        mWriter.write(String.format(
                "To suppress this error, use the issue id \"%1$s\" as explained in the " +
                "%2$sSuppressing Warnings and Errors%3$s section.",
                issue.getId(),
                "<a href=\"#SuppressInfo\">", "</a>"));          //$NON-NLS-1$ //$NON-NLS-2$
        mWriter.write("<br/>\n");
    }

    private void writeSuppressInfo() throws IOException {
        //getSuppressHelp
        mWriter.write("\n<a name=\"SuppressInfo\"></a>\n");      //$NON-NLS-1$
        mWriter.write("<div class=\"category\">");               //$NON-NLS-1$
        mWriter.write("Suppressing Warnings and Errors");
        mWriter.write("<div class=\"categorySeparator\"></div>\n");//$NON-NLS-1$
        mWriter.write("</div>\n");                               //$NON-NLS-1$
        mWriter.write(TextFormat.RAW.convertTo(Main.getSuppressHelp(), TextFormat.HTML));
        mWriter.write('\n');
    }

    protected Map<Issue, String> computeMissingIssues(List<Warning> warnings) {
        Set<Project> projects = new HashSet<Project>();
        Set<Issue> seen = new HashSet<Issue>();
        for (Warning warning : warnings) {
            projects.add(warning.project);
            seen.add(warning.issue);
        }
        Configuration cliConfiguration = mClient.getConfiguration();
        Map<Issue, String> map = Maps.newHashMap();
        for (Issue issue : mClient.getRegistry().getIssues()) {
            if (!seen.contains(issue)) {
                if (mClient.isSuppressed(issue)) {
                    map.put(issue, "Command line flag");
                    continue;
                }

                if (!issue.isEnabledByDefault() && !mClient.isAllEnabled()) {
                    map.put(issue, "Default");
                    continue;
                }

                if (cliConfiguration != null && !cliConfiguration.isEnabled(issue)) {
                    map.put(issue, "Command line supplied --config lint.xml file");
                    continue;
                }

                // See if any projects disable this warning
                for (Project project : projects) {
                    if (!project.getConfiguration(null).isEnabled(issue)) {
                        map.put(issue, "Project lint.xml file");
                        break;
                    }
                }
            }
        }

        return map;
    }

    private void writeMissingIssues(Map<Issue, String> missing) throws IOException {
        mWriter.write("\n<a name=\"MissingIssues\"></a>\n");        //$NON-NLS-1$
        mWriter.write("<div class=\"category\">");                  //$NON-NLS-1$
        mWriter.write("Disabled Checks");
        mWriter.write("<div class=\"categorySeparator\"></div>\n"); //$NON-NLS-1$
        mWriter.write("</div>\n");                                  //$NON-NLS-1$

        mWriter.write(
                "The following issues were not run by lint, either " +
                "because the check is not enabled by default, or because " +
                "it was disabled with a command line flag or via one or " +
                "more lint.xml configuration files in the project directories.");
        mWriter.write("\n<br/><br/>\n"); //$NON-NLS-1$

        List<Issue> list = new ArrayList<Issue>(missing.keySet());
        Collections.sort(list);


        for (Issue issue : list) {
            mWriter.write("<a name=\"" + issue.getId() + "\"></a>\n"); //$NON-NLS-1$ //$NON-NLS-2$
            mWriter.write("<div class=\"issue\">\n");                  //$NON-NLS-1$

            // Explain this issue
            mWriter.write("<div class=\"id\">");                       //$NON-NLS-1$
            mWriter.write(issue.getId());
            mWriter.write("<div class=\"issueSeparator\"></div>\n");   //$NON-NLS-1$
            mWriter.write("</div>\n");                                 //$NON-NLS-1$
            String disabledBy = missing.get(issue);
            writeIssueMetadata(issue, issue.getDefaultSeverity(), disabledBy);
            mWriter.write("</div>\n");                                 //$NON-NLS-1$
        }
    }

    protected void writeStyleSheet() throws IOException {
        if (USE_HOLO_STYLE) {
            mWriter.write(
                "<link rel=\"stylesheet\" type=\"text/css\" " +          //$NON-NLS-1$
                "href=\"http://fonts.googleapis.com/css?family=Roboto\" />\n" );//$NON-NLS-1$
        }

        URL cssUrl = HtmlReporter.class.getResource(CSS);
        if (mSimpleFormat) {
            // Inline the CSS
            mWriter.write("<style>\n");                                   //$NON-NLS-1$
            InputStream input = cssUrl.openStream();
            byte[] bytes = ByteStreams.toByteArray(input);
            try {
                Closeables.close(input, true /* swallowIOException */);
            } catch (IOException e) {
                // cannot happen
            }
            String css = new String(bytes, Charsets.UTF_8);
            mWriter.write(css);
            mWriter.write("</style>\n");                                  //$NON-NLS-1$
        } else {
            String ref = addLocalResources(cssUrl);
            if (ref != null) {
                mWriter.write(
                "<link rel=\"stylesheet\" type=\"text/css\" href=\""     //$NON-NLS-1$
                            + ref + "\" />\n");                          //$NON-NLS-1$
            }
        }
    }

    private void writeOverview(List<List<Warning>> related, int missingCount)
            throws IOException {
        // Write issue id summary
        mWriter.write("<table class=\"overview\">\n");                          //$NON-NLS-1$

        String errorUrl = null;
        String warningUrl = null;
        if (!mSimpleFormat) {
            errorUrl = addLocalResources(getErrorIconUrl());
            warningUrl = addLocalResources(getWarningIconUrl());
            mFixUrl = addLocalResources(HtmlReporter.class.getResource("lint-run.png")); //$NON-NLS-1$)
        }

        Category previousCategory = null;
        for (List<Warning> warnings : related) {
            Issue issue = warnings.get(0).issue;

            boolean isError = false;
            for (Warning warning : warnings) {
                if (warning.severity == Severity.ERROR || warning.severity == Severity.FATAL) {
                    isError = true;
                    break;
                }
            }

            if (issue.getCategory() != previousCategory) {
                mWriter.write("<tr><td></td><td class=\"categoryColumn\">");
                previousCategory = issue.getCategory();
                String categoryName = issue.getCategory().getFullName();
                mWriter.write("<a href=\"#");                        //$NON-NLS-1$
                mWriter.write(categoryName);
                mWriter.write("\">");                                //$NON-NLS-1$
                mWriter.write(categoryName);
                mWriter.write("</a>\n");                             //$NON-NLS-1$
                mWriter.write("</td></tr>");                         //$NON-NLS-1$
                mWriter.write("\n");                                 //$NON-NLS-1$
            }
            mWriter.write("<tr>\n");                                 //$NON-NLS-1$

            // Count column
            mWriter.write("<td class=\"countColumn\">");             //$NON-NLS-1$
            mWriter.write(Integer.toString(warnings.size()));
            mWriter.write("</td>");                                  //$NON-NLS-1$

            mWriter.write("<td class=\"issueColumn\">");             //$NON-NLS-1$

            String imageUrl = isError ? errorUrl : warningUrl;
            if (imageUrl != null) {
                mWriter.write("<img border=\"0\" align=\"top\" src=\""); //$NON-NLS-1$
                mWriter.write(imageUrl);
                mWriter.write("\" alt=\"");
                mWriter.write(isError ? "Error" : "Warning");
                mWriter.write("\" />\n");                            //$NON-NLS-1$
            }

            mWriter.write("<a href=\"#");                            //$NON-NLS-1$
            mWriter.write(issue.getId());
            mWriter.write("\">");                                    //$NON-NLS-1$
            mWriter.write(issue.getId());
            mWriter.write(": ");                                     //$NON-NLS-1$
            mWriter.write(issue.getBriefDescription(HTML));
            mWriter.write("</a>\n");                                 //$NON-NLS-1$

            mWriter.write("</td></tr>\n");
        }

        if (missingCount > 0 && !mClient.isCheckingSpecificIssues()) {
            mWriter.write("<tr><td></td>");                          //$NON-NLS-1$
            mWriter.write("<td class=\"categoryColumn\">");          //$NON-NLS-1$
            mWriter.write("<a href=\"#MissingIssues\">");            //$NON-NLS-1$
            mWriter.write(String.format("Disabled Checks (%1$d)",
                    missingCount));

            mWriter.write("</a>\n");                                 //$NON-NLS-1$
            mWriter.write("</td></tr>");                             //$NON-NLS-1$
        }

        mWriter.write("</table>\n");                                 //$NON-NLS-1$
        mWriter.write("<br/>");                                      //$NON-NLS-1$
    }

    private String writeLocation(File file, String path, int line) throws IOException {
        String url;
        mWriter.write("<span class=\"location\">");      //$NON-NLS-1$

        url = getUrl(file);
        if (url != null) {
            mWriter.write("<a href=\"");                 //$NON-NLS-1$
            mWriter.write(url);
            mWriter.write("\">");                        //$NON-NLS-1$
        }

        String displayPath = stripPath(path);
        if (url != null && url.startsWith("../") && new File(displayPath).isAbsolute()) {
            displayPath = url;
        }
        mWriter.write(displayPath);
        //noinspection VariableNotUsedInsideIf
        if (url != null) {
            mWriter.write("</a>");                       //$NON-NLS-1$
        }
        if (line >= 0) {
            // 0-based line numbers, but display 1-based
            mWriter.write(':');
            mWriter.write(Integer.toString(line + 1));
        }
        mWriter.write("</span>");                        //$NON-NLS-1$
        return url;
    }

    private boolean addImage(String url, Location location) throws IOException {
        if (url != null && endsWith(url, DOT_PNG)) {
            if (location.getSecondary() != null) {
                // Emit many images
                // Add in linked images as well
                List<String> urls = new ArrayList<String>();
                while (location != null && location.getFile() != null) {
                    String imageUrl = getUrl(location.getFile());
                    if (imageUrl != null
                            && endsWith(imageUrl, DOT_PNG)) {
                        urls.add(imageUrl);
                    }
                    location = location.getSecondary();
                }
                if (!urls.isEmpty()) {
                    // Sort in order
                    Collections.sort(urls, new Comparator<String>() {
                        @Override
                        public int compare(String s1, String s2) {
                            return getDpiRank(s1) - getDpiRank(s2);
                        }
                    });
                    mWriter.write("<table>");                            //$NON-NLS-1$
                    mWriter.write("<tr>");                               //$NON-NLS-1$
                    for (String linkedUrl : urls) {
                        // Image series: align top
                        mWriter.write("<td>");                           //$NON-NLS-1$
                        mWriter.write("<a href=\"");                     //$NON-NLS-1$
                        mWriter.write(linkedUrl);
                        mWriter.write("\">");                            //$NON-NLS-1$
                        mWriter.write("<img border=\"0\" align=\"top\" src=\"");      //$NON-NLS-1$
                        mWriter.write(linkedUrl);
                        mWriter.write("\" /></a>\n");                    //$NON-NLS-1$
                        mWriter.write("</td>");                          //$NON-NLS-1$
                    }
                    mWriter.write("</tr>");                              //$NON-NLS-1$

                    mWriter.write("<tr>");                               //$NON-NLS-1$
                    for (String linkedUrl : urls) {
                        mWriter.write("<th>");                           //$NON-NLS-1$
                        int index = linkedUrl.lastIndexOf("drawable-");  //$NON-NLS-1$
                        if (index != -1) {
                            index += "drawable-".length();               //$NON-NLS-1$
                            int end = linkedUrl.indexOf('/', index);
                            if (end != -1) {
                                mWriter.write(linkedUrl.substring(index, end));
                            }
                        }
                        mWriter.write("</th>");                          //$NON-NLS-1$
                    }
                    mWriter.write("</tr>\n");                            //$NON-NLS-1$

                    mWriter.write("</table>\n");                         //$NON-NLS-1$
                }
            } else {
                // Just this image: float to the right
                mWriter.write("<img class=\"embedimage\" align=\"right\" src=\""); //$NON-NLS-1$
                mWriter.write(url);
                mWriter.write("\" />");                                  //$NON-NLS-1$
            }

            return true;
        }

        return false;
    }

    /** Provide a sorting rank for a url */
    private static int getDpiRank(String url) {
        if (url.contains("-xhdpi")) {                                   //$NON-NLS-1$
            return 0;
        } else if (url.contains("-hdpi")) {                             //$NON-NLS-1$
            return 1;
        } else if (url.contains("-mdpi")) {                             //$NON-NLS-1$
            return 2;
        } else if (url.contains("-ldpi")) {                             //$NON-NLS-1$
            return 3;
        } else {
            return 4;
        }
    }

    private void appendCodeBlock(String contents, int lineno, int offset)
            throws IOException {
        int max = lineno + 3;
        int min = lineno - 3;
        for (int l = min; l < max; l++) {
            if (l >= 0) {
                int lineOffset = LintCliClient.getLineOffset(contents, l);
                if (lineOffset == -1) {
                    break;
                }

                mWriter.write(String.format("<span class=\"lineno\">%1$4d</span> ", (l + 1))); //$NON-NLS-1$

                String line = LintCliClient.getLineOfOffset(contents, lineOffset);
                if (offset != -1 && lineOffset <= offset && lineOffset+line.length() >= offset) {
                    // Text nodes do not always have correct lines/offsets
                    //assert l == lineno;

                    // This line contains the beginning of the offset
                    // First print everything before
                    int delta = offset - lineOffset;
                    appendEscapedText(line.substring(0, delta));
                    mWriter.write("<span class=\"errorspan\">");         //$NON-NLS-1$
                    appendEscapedText(line.substring(delta));
                    mWriter.write("</span>");                            //$NON-NLS-1$
                } else if (offset == -1 && l == lineno) {
                    mWriter.write("<span class=\"errorline\">");         //$NON-NLS-1$
                    appendEscapedText(line);
                    mWriter.write("</span>");                            //$NON-NLS-1$
                } else {
                    appendEscapedText(line);
                }
                if (l < max - 1) {
                    mWriter.write("\n");                                 //$NON-NLS-1$
                }
            }
        }
    }

    protected void appendEscapedText(String textValue) throws IOException {
        for (int i = 0, n = textValue.length(); i < n; i++) {
            char c = textValue.charAt(i);
            if (c == '<') {
                mWriter.write("&lt;");                                   //$NON-NLS-1$
            } else if (c == '&') {
                mWriter.write("&amp;");                                  //$NON-NLS-1$
            } else if (c == '\n') {
                mWriter.write("<br/>\n");
            } else {
                if (c > 255) {
                    mWriter.write("&#");                                 //$NON-NLS-1$
                    mWriter.write(Integer.toString(c));
                    mWriter.write(';');
                } else {
                    mWriter.write(c);
                }
            }
        }
    }

    private String stripPath(String path) {
        if (mStripPrefix != null && path.startsWith(mStripPrefix)
                && path.length() > mStripPrefix.length()) {
            int index = mStripPrefix.length();
            if (path.charAt(index) == File.separatorChar) {
                index++;
            }
            return path.substring(index);
        }

        return path;
    }

    /** Sets path prefix to strip from displayed file names */
    void setStripPrefix(String prefix) {
        mStripPrefix = prefix;
    }

    static URL getWarningIconUrl() {
        return HtmlReporter.class.getResource("lint-warning.png");   //$NON-NLS-1$
    }

    static URL getErrorIconUrl() {
        return HtmlReporter.class.getResource("lint-error.png");     //$NON-NLS-1$
    }
}
