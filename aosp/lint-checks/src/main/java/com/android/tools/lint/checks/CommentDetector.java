/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.tools.lint.checks;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.Speed;

import java.io.File;
import java.util.Collections;
import java.util.List;

import lombok.ast.AstVisitor;
import lombok.ast.Comment;
import lombok.ast.ForwardingAstVisitor;
import lombok.ast.Node;

/**
 * Looks for issues in Java comments
 */
public class CommentDetector extends Detector implements Detector.JavaScanner {
    private static final String STOPSHIP_COMMENT = "STOPSHIP"; //$NON-NLS-1$

    private static final Implementation IMPLEMENTATION = new Implementation(
            CommentDetector.class,
            Scope.JAVA_FILE_SCOPE);

    /** Looks for hidden code */
    public static final Issue EASTER_EGG = Issue.create(
            "EasterEgg", //$NON-NLS-1$
            "Code contains easter egg",
            "An \"easter egg\" is code deliberately hidden in the code, both from potential " +
            "users and even from other developers. This lint check looks for code which " +
            "looks like it may be hidden from sight.",
            Category.SECURITY,
            6,
            Severity.WARNING,
            IMPLEMENTATION)
            .setEnabledByDefault(false);

    /** Looks for special comment markers intended to stop shipping the code */
    public static final Issue STOP_SHIP = Issue.create(
            "StopShip", //$NON-NLS-1$
            "Code contains `STOPSHIP` marker",

            "Using the comment `// STOPSHIP` can be used to flag code that is incomplete but " +
            "checked in. This comment marker can be used to indicate that the code should not " +
            "be shipped until the issue is addressed, and lint will look for these.",
            Category.CORRECTNESS,
            10,
            Severity.WARNING,
            IMPLEMENTATION)
            .setEnabledByDefault(false);

    private static final String ESCAPE_STRING = "\\u002a\\u002f"; //$NON-NLS-1$

    /** Lombok's AST only passes comment nodes for Javadoc so I need to do manual token scanning
         instead */
    private static final boolean USE_AST = false;


    /** Constructs a new {@link CommentDetector} check */
    public CommentDetector() {
    }

    @Override
    public boolean appliesTo(@NonNull Context context, @NonNull File file) {
        return true;
    }

    @NonNull
    @Override
    public Speed getSpeed() {
        return Speed.NORMAL;
    }

    @Override
    public List<Class<? extends Node>> getApplicableNodeTypes() {
        if (USE_AST) {
            return Collections.<Class<? extends Node>>singletonList(Comment.class);
        } else {
            return null;
        }
    }

    @Override
    public AstVisitor createJavaVisitor(@NonNull JavaContext context) {
        // Lombok does not generate comment nodes for block and line comments, only for
        // javadoc comments!
        if (USE_AST) {
            return new CommentChecker(context);
        } else {
            String source = context.getContents();
            if (source == null) {
                return null;
            }
            // Process the Java source such that we pass tokens to it

            for (int i = 0, n = source.length() - 1; i < n; i++) {
                char c = source.charAt(i);
                if (c == '\\') {
                    i += 1;
                } else if (c == '/') {
                    char next = source.charAt(i + 1);
                    if (next == '/') {
                        // Line comment
                        int start = i + 2;
                        int end = source.indexOf('\n', start);
                        if (end == -1) {
                            end = n;
                        }
                        checkComment(context, null, source, 0, start, end);
                    } else if (next == '*') {
                        // Block comment
                        int start = i + 2;
                        int end = source.indexOf("*/", start);
                        if (end == -1) {
                            end = n;
                        }
                        checkComment(context, null, source, 0, start, end);
                    }
                }
            }
            return null;
        }
    }

    private static class CommentChecker extends ForwardingAstVisitor {
        private final JavaContext mContext;

        public CommentChecker(JavaContext context) {
            mContext = context;
        }

        @Override
        public boolean visitComment(Comment node) {
            String contents = node.astContent();
            checkComment(mContext, node, contents, node.getPosition().getStart(), 0, contents.length());
            return super.visitComment(node);
        }
    }

    private static void checkComment(
            @NonNull JavaContext context,
            @Nullable Comment node,
            @NonNull String source,
            int offset,
            int start,
            int end) {
        char prev = 0;
        char c;
        for (int i = start; i < end - 2; i++, prev = c) {
            c = source.charAt(i);
            if (prev == '\\') {
                if (c == 'u' || c == 'U') {
                    if (source.regionMatches(true, i - 1, ESCAPE_STRING,
                            0, ESCAPE_STRING.length())) {
                        Location location = Location.create(context.file, source,
                                offset + i - 1, offset + i - 1 + ESCAPE_STRING.length());
                        context.report(EASTER_EGG, node, location,
                                "Code might be hidden here; found unicode escape sequence " +
                                "which is interpreted as comment end, compiled code follows");
                    }
                } else {
                    i++;
                }
            } else if (prev == 'S' && c == 'T' &&
                    source.regionMatches(i - 1, STOPSHIP_COMMENT, 0, STOPSHIP_COMMENT.length())) {
                // TODO: Only flag this issue in release mode??
                Location location = Location.create(context.file, source,
                        offset + i - 1, offset + i - 1 + STOPSHIP_COMMENT.length());
                context.report(STOP_SHIP, node, location,
                        "`STOPSHIP` comment found; points to code which must be fixed prior " +
                        "to release");
            }
        }
    }
}
