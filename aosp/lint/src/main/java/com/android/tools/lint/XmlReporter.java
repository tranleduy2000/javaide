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

import static com.android.tools.lint.detector.api.TextFormat.RAW;

import com.android.tools.lint.checks.BuiltinIssueRegistry;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.Position;
import com.google.common.annotations.Beta;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.Files;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * A reporter which emits lint results into an XML report.
 * <p>
 * <b>NOTE: This is not a public or final API; if you rely on this be prepared
 * to adjust your code for the next tools release.</b>
 */
@Beta
public class XmlReporter extends Reporter {
    private final Writer mWriter;

    /**
     * Constructs a new {@link XmlReporter}
     *
     * @param client the client
     * @param output the output file
     * @throws IOException if an error occurs
     */
    public XmlReporter(LintCliClient client, File output) throws IOException {
        super(client, output);
        mWriter = new BufferedWriter(Files.newWriter(output, Charsets.UTF_8));
    }

    @Override
    public void write(int errorCount, int warningCount, List<Warning> issues) throws IOException {
        mWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");      //$NON-NLS-1$
        // Format 4: added urls= attribute with all more info links, comma separated
        mWriter.write("<issues format=\"4\"");                              //$NON-NLS-1$
        String revision = mClient.getRevision();
        if (revision != null) {
            mWriter.write(String.format(" by=\"lint %1$s\"", revision));    //$NON-NLS-1$
        }
        mWriter.write(">\n");                                               //$NON-NLS-1$

        if (!issues.isEmpty()) {
            for (Warning warning : issues) {
                mWriter.write('\n');
                indent(mWriter, 1);
                mWriter.write("<issue"); //$NON-NLS-1$
                Issue issue = warning.issue;
                writeAttribute(mWriter, 2, "id", issue.getId());                      //$NON-NLS-1$
                writeAttribute(mWriter, 2, "severity",
                        warning.severity.getDescription());
                writeAttribute(mWriter, 2, "message", warning.message);               //$NON-NLS-1$

                writeAttribute(mWriter, 2, "category",                                //$NON-NLS-1$
                        issue.getCategory().getFullName());
                writeAttribute(mWriter, 2, "priority",                                //$NON-NLS-1$
                        Integer.toString(issue.getPriority()));
                writeAttribute(mWriter, 2, "summary", issue.getBriefDescription(RAW));//$NON-NLS-1$
                writeAttribute(mWriter, 2, "explanation", issue.getExplanation(RAW)); //$NON-NLS-1$
                List<String> moreInfo = issue.getMoreInfo();
                if (!moreInfo.isEmpty()) {
                    // Compatibility with old format: list first URL
                    writeAttribute(mWriter, 2, "url", moreInfo.get(0));               //$NON-NLS-1$
                    writeAttribute(mWriter, 2, "urls",                                //$NON-NLS-1$
                            Joiner.on(',').join(issue.getMoreInfo()));
                }
                if (warning.errorLine != null && !warning.errorLine.isEmpty()) {
                    String line = warning.errorLine;
                    int index1 = line.indexOf('\n');
                    if (index1 != -1) {
                        int index2 = line.indexOf('\n', index1 + 1);
                        if (index2 != -1) {
                            String line1 = line.substring(0, index1);
                            String line2 = line.substring(index1 + 1, index2);
                            writeAttribute(mWriter, 2, "errorLine1", line1);          //$NON-NLS-1$
                            writeAttribute(mWriter, 2, "errorLine2", line2);          //$NON-NLS-1$
                        }
                    }
                }

                if (warning.isVariantSpecific()) {
                    writeAttribute(mWriter, 2, "includedVariants", Joiner.on(',').join(warning.getIncludedVariantNames()));
                    writeAttribute(mWriter, 2, "excludedVariants", Joiner.on(',').join(warning.getExcludedVariantNames()));
                }

                if (mClient.getRegistry() instanceof BuiltinIssueRegistry) {
                    boolean adt = QuickfixHandler.ADT.hasAutoFix(issue);
                    boolean studio = QuickfixHandler.STUDIO.hasAutoFix(issue);
                    if (adt || studio) { //$NON-NLS-1$
                        String value = adt && studio ? "studio,adt" : studio ? "studio" : "adt";
                        writeAttribute(mWriter, 2, "quickfix", value);      //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }

                assert (warning.file != null) == (warning.location != null);

                if (warning.file != null) {
                    assert warning.location.getFile() == warning.file;
                }

                Location location = warning.location;
                if (location != null) {
                    mWriter.write(">\n"); //$NON-NLS-1$
                    while (location != null) {
                        indent(mWriter, 2);
                        mWriter.write("<location"); //$NON-NLS-1$
                        String path = mClient.getDisplayPath(warning.project, location.getFile());
                        writeAttribute(mWriter, 3, "file", path);  //$NON-NLS-1$
                        Position start = location.getStart();
                        if (start != null) {
                            int line = start.getLine();
                            int column = start.getColumn();
                            if (line >= 0) {
                                // +1: Line numbers internally are 0-based, report should be
                                // 1-based.
                                writeAttribute(mWriter, 3, "line",         //$NON-NLS-1$
                                        Integer.toString(line + 1));
                                if (column >= 0) {
                                    writeAttribute(mWriter, 3, "column",   //$NON-NLS-1$
                                            Integer.toString(column + 1));
                                }
                            }
                        }

                        mWriter.write("/>\n"); //$NON-NLS-1$
                        location = location.getSecondary();
                    }
                    indent(mWriter, 1);
                    mWriter.write("</issue>\n"); //$NON-NLS-1$
                } else {
                    mWriter.write('\n');
                    indent(mWriter, 1);
                    mWriter.write("/>\n");  //$NON-NLS-1$
                }
            }
        }

        mWriter.write("\n</issues>\n");       //$NON-NLS-1$
        mWriter.close();

        if (!mClient.getFlags().isQuiet()
                && (mDisplayEmpty || errorCount > 0 || warningCount > 0)) {
            String path = mOutput.getAbsolutePath();
            System.out.println(String.format("Wrote XML report to %1$s", path));
        }
    }

    private static void writeAttribute(Writer writer, int indent, String name, String value)
            throws IOException {
        writer.write('\n');
        indent(writer, indent);
        writer.write(name);
        writer.write('=');
        writer.write('"');
        for (int i = 0, n = value.length(); i < n; i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':
                    writer.write("&quot;"); //$NON-NLS-1$
                    break;
                case '\'':
                    writer.write("&apos;"); //$NON-NLS-1$
                    break;
                case '&':
                    writer.write("&amp;");  //$NON-NLS-1$
                    break;
                case '<':
                    writer.write("&lt;");   //$NON-NLS-1$
                    break;
                default:
                    writer.write(c);
                    break;
            }
        }
        writer.write('"');
    }

    private static void indent(Writer writer, int indent) throws IOException {
        for (int level = 0; level < indent; level++) {
            writer.write("    "); //$NON-NLS-1$
        }
    }
}