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
import static com.android.tools.lint.detector.api.TextFormat.TEXT;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.Position;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.TextFormat;
import com.android.utils.SdkUtils;
import com.google.common.annotations.Beta;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * A reporter which emits lint warnings as plain text strings
 * <p>
 * <b>NOTE: This is not a public or final API; if you rely on this be prepared
 * to adjust your code for the next tools release.</b>
 */
@Beta
public class TextReporter extends Reporter {
    private final Writer mWriter;
    private final boolean mClose;
    private final LintCliFlags mFlags;

    /**
     * Constructs a new {@link TextReporter}
     *
     * @param client the client
     * @param flags the flags
     * @param writer the writer to write into
     * @param close whether the writer should be closed when done
     */
    public TextReporter(@NonNull LintCliClient client, @NonNull LintCliFlags flags,
            @NonNull Writer writer, boolean close) {
        this(client, flags, null, writer, close);
    }

    /**
     * Constructs a new {@link TextReporter}
     *
     * @param client the client
     * @param flags the flags
     * @param file the file corresponding to the writer, if any
     * @param writer the writer to write into
     * @param close whether the writer should be closed when done
     */
    public TextReporter(@NonNull LintCliClient client, @NonNull LintCliFlags flags,
            @Nullable File file, @NonNull Writer writer, boolean close) {
        super(client, file);
        mFlags = flags;
        mWriter = writer;
        mClose = close;
    }

    @Override
    public void write(int errorCount, int warningCount, List<Warning> issues) throws IOException {
        boolean abbreviate = !mFlags.isShowEverything();

        StringBuilder output = new StringBuilder(issues.size() * 200);
        if (issues.isEmpty()) {
            if (mDisplayEmpty) {
                mWriter.write("No issues found.");
                mWriter.write('\n');
                mWriter.flush();
            }
        } else {
            Issue lastIssue = null;
            for (Warning warning : issues) {
                if (warning.issue != lastIssue) {
                    explainIssue(output, lastIssue);
                    lastIssue = warning.issue;
                }
                int startLength = output.length();

                if (warning.path != null) {
                    output.append(warning.path);
                    output.append(':');

                    if (warning.line >= 0) {
                        output.append(Integer.toString(warning.line + 1));
                        output.append(':');
                    }
                    if (startLength < output.length()) {
                        output.append(' ');
                    }
                }

                Severity severity = warning.severity;
                if (severity == Severity.FATAL) {
                    // Treat the fatal error as an error such that we don't display
                    // both "Fatal:" and "Error:" etc in the error output.
                    severity = Severity.ERROR;
                }
                output.append(severity.getDescription());
                output.append(':');
                output.append(' ');

                output.append(RAW.convertTo(warning.message, TEXT));

                if (warning.issue != null) {
                    output.append(' ').append('[');
                    output.append(warning.issue.getId());
                    output.append(']');
                }

                output.append('\n');

                if (warning.errorLine != null && !warning.errorLine.isEmpty()) {
                    output.append(warning.errorLine);
                }

                if (warning.location != null && warning.location.getSecondary() != null) {
                    Location location = warning.location.getSecondary();
                    boolean omitted = false;
                    while (location != null) {
                        if (location.getMessage() != null
                                && !location.getMessage().isEmpty()) {
                            output.append("    "); //$NON-NLS-1$
                            String path = mClient.getDisplayPath(warning.project,
                                    location.getFile());
                            output.append(path);

                            Position start = location.getStart();
                            if (start != null) {
                                int line = start.getLine();
                                if (line >= 0) {
                                    output.append(':');
                                    output.append(Integer.toString(line + 1));
                                }
                            }

                            if (location.getMessage() != null
                                    && !location.getMessage().isEmpty()) {
                                output.append(':');
                                output.append(' ');
                                output.append(RAW.convertTo(location.getMessage(), TEXT));
                            }

                            output.append('\n');
                        } else {
                            omitted = true;
                        }

                        location = location.getSecondary();
                    }

                    if (!abbreviate && omitted) {
                        location = warning.location.getSecondary();
                        StringBuilder sb = new StringBuilder(100);
                        sb.append("Also affects: ");
                        int begin = sb.length();
                        while (location != null) {
                            if (location.getMessage() == null
                                    || location.getMessage().isEmpty()) {
                                if (sb.length() > begin) {
                                    sb.append(", ");
                                }

                                String path = mClient.getDisplayPath(warning.project,
                                        location.getFile());
                                sb.append(path);

                                Position start = location.getStart();
                                if (start != null) {
                                    int line = start.getLine();
                                    if (line >= 0) {
                                        sb.append(':');
                                        sb.append(Integer.toString(line + 1));
                                    }
                                }
                            }

                            location = location.getSecondary();
                        }
                        String wrapped = Main.wrap(sb.toString(), Main.MAX_LINE_WIDTH, "     "); //$NON-NLS-1$
                        output.append(wrapped);
                    }
                }

                if (warning.isVariantSpecific()) {
                    List<String> names;
                    if (warning.includesMoreThanExcludes()) {
                        output.append("Applies to variants: ");
                        names = warning.getIncludedVariantNames();
                    } else {
                        output.append("Does not apply to variants: ");
                        names = warning.getExcludedVariantNames();
                    }
                    output.append(Joiner.on(", ").join(names));
                    output.append('\n');
                }
            }
            explainIssue(output, lastIssue);

            mWriter.write(output.toString());

            mWriter.write(String.format("%1$d errors, %2$d warnings",
                    errorCount, warningCount));
            mWriter.write('\n');
            mWriter.flush();
            if (mClose) {
                mWriter.close();

                if (!mClient.getFlags().isQuiet() && mOutput != null) {
                    String path = mOutput.getAbsolutePath();
                    System.out.println(String.format("Wrote text report to %1$s", path));
                }
            }
        }
    }

    private void explainIssue(@NonNull StringBuilder output, @Nullable Issue issue)
            throws IOException {
        if (issue == null || !mFlags.isExplainIssues() || issue == IssueRegistry.LINT_ERROR) {
            return;
        }

        String explanation = issue.getExplanation(TextFormat.TEXT);
        if (explanation.trim().isEmpty()) {
            return;
        }

        String indent = "   ";
        String formatted = SdkUtils.wrap(explanation, Main.MAX_LINE_WIDTH - indent.length(), null);
        output.append('\n');
        output.append(indent);
        output.append("Explanation for issues of type \"").append(issue.getId()).append("\":\n");
        for (String line : Splitter.on('\n').split(formatted)) {
            if (!line.isEmpty()) {
                output.append(indent);
                output.append(line);
            }
            output.append('\n');
        }
        List<String> moreInfo = issue.getMoreInfo();
        if (!moreInfo.isEmpty()) {
            for (String url : moreInfo) {
                if (formatted.contains(url)) {
                    continue;
                }
                output.append(indent);
                output.append(url);
                output.append('\n');
            }
            output.append('\n');
        }
    }

    boolean isWriteToConsole() {
        return mOutput == null;
    }
}