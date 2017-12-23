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

package com.android.sdklib.repository;

import com.android.annotations.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Package multi-part revision number composed of a tuple
 * (major.minor.micro) and an optional preview revision
 * (the lack of a preview number indicates it's not a preview
 *  but a final package.)
 *
 *  @see MajorRevision
 */
public class FullRevision implements Comparable<FullRevision> {

    public static final int MISSING_MAJOR_REV  = 0;
    public static final int IMPLICIT_MINOR_REV = 0;
    public static final int IMPLICIT_MICRO_REV = 0;
    public static final int NOT_A_PREVIEW      = 0;

    public static final FullRevision NOT_SPECIFIED = new FullRevision(MISSING_MAJOR_REV);

    private static final Pattern FULL_REVISION_PATTERN =
        //                   1=major       2=minor       3=micro              4=preview
        Pattern.compile("\\s*([0-9]+)(?:\\.([0-9]+)(?:\\.([0-9]+))?)?([\\s-]*)?(?:rc([0-9]+))?\\s*");

    private final int mMajor;
    private final int mMinor;
    private final int mMicro;
    private final int mPreview;
    private final String mPreviewSeparator;

    public FullRevision(int major) {
        this(major, IMPLICIT_MINOR_REV, IMPLICIT_MICRO_REV);
    }

    public FullRevision(int major, int minor, int micro) {
        this(major, minor, micro, NOT_A_PREVIEW);
    }

    public FullRevision(int major, int minor, int micro, int preview) {
      this(major, minor, micro, preview, " ");
    }

    public FullRevision(int major, int minor, int micro, int preview, String previewSeparator) {
        mMajor = major;
        mMinor = minor;
        mMicro = micro;
        mPreview = preview;
        mPreviewSeparator = previewSeparator;
    }

    public int getMajor() {
        return mMajor;
    }

    public int getMinor() {
        return mMinor;
    }

    public int getMicro() {
        return mMicro;
    }

    public boolean isPreview() {
        return mPreview > NOT_A_PREVIEW;
    }

    public int getPreview() {
        return mPreview;
    }

    /**
     * Parses a string of format "major.minor.micro rcPreview" and returns
     * a new {@link FullRevision} for it. All the fields except major are
     * optional.
     * <p/>
     * The parsing is equivalent to the pseudo-BNF/regexp:
     * <pre>
     *   Major/Minor/Micro/Preview := [0-9]+
     *   Revision := Major ('.' Minor ('.' Micro)? )? \s* ('rc'Preview)?
     * </pre>
     *
     * @param revision A non-null revision to parse.
     * @return A new non-null {@link FullRevision}.
     * @throws NumberFormatException if the parsing failed.
     */
    @NonNull
    public static FullRevision parseRevision(@NonNull String revision)
            throws NumberFormatException {
        return parseRevisionImpl(revision, true /*supportMinorMicro*/, true /*supportPreview*/);
    }

    @NonNull
    protected static FullRevision parseRevisionImpl(@NonNull String revision,
                                                    boolean supportMinorMicro,
                                                    boolean supportPreview)
                                  throws NumberFormatException {
        if (revision == null) {
            throw new NumberFormatException("revision is <null>"); //$NON-NLS-1$
        }

        Throwable cause = null;
        String error = null;
        try {
            Matcher m = FULL_REVISION_PATTERN.matcher(revision);
            if (m != null && m.matches()) {
                int major = Integer.parseInt(m.group(1));

                int minor = IMPLICIT_MINOR_REV;
                int micro = IMPLICIT_MICRO_REV;
                int preview = NOT_A_PREVIEW;
                String previewSeparator = " ";

                String s = m.group(2);
                if (s != null) {
                    if (!supportMinorMicro) {
                        error = " -- Minor number not supported";   //$NON-NLS-1$
                    } else {
                        minor = Integer.parseInt(s);
                    }
                }

                s = m.group(3);
                if (s != null) {
                    if (!supportMinorMicro) {
                        error = " -- Micro number not supported";   //$NON-NLS-1$
                    } else {
                        micro = Integer.parseInt(s);
                    }
                }

                s = m.group(5); // Group 4 is the preview separator
                if (s != null) {
                    if (!supportPreview) {
                        error = " -- Preview number not supported";   //$NON-NLS-1$
                    } else {
                        preview = Integer.parseInt(s);
                        previewSeparator = m.group(4);
                    }
                }

                if (error == null) {
                    return new FullRevision(major, minor, micro, preview, previewSeparator);
                }
            }
        } catch (Throwable t) {
            cause = t;
        }

        NumberFormatException n = new NumberFormatException(
                "Invalid revision: "        //$NON-NLS-1$
                + revision
                + (error == null ? "" : error));
        if (cause != null) {
            n.initCause(cause);
        }
        throw n;
    }

    /**
     * Returns the version in a fixed format major.minor.micro
     * with an optional "rc preview#". For example it would
     * return "18.0.0", "18.1.0" or "18.1.2 rc5".
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(mMajor)
          .append('.').append(mMinor)
          .append('.').append(mMicro);

        if (mPreview != NOT_A_PREVIEW) {
            sb.append(mPreviewSeparator).append("rc").append(mPreview);
        }

        return sb.toString();
    }

    /**
     * Returns the version in a dynamic format "major.minor.micro rc#".
     * This is similar to {@link #toString()} except it omits minor, micro
     * or preview versions when they are zero.
     * For example it would return "18 rc1" instead of "18.0.0 rc1",
     * or "18.1 rc2" instead of "18.1.0 rc2".
     */
    public String toShortString() {
        StringBuilder sb = new StringBuilder();
        sb.append(mMajor);
        if (mMinor > 0 || mMicro > 0) {
            sb.append('.').append(mMinor);
        }
        if (mMicro > 0) {
            sb.append('.').append(mMicro);
        }
        if (mPreview != NOT_A_PREVIEW) {
            sb.append(mPreviewSeparator).append("rc").append(mPreview);
        }

        return sb.toString();
    }

    /**
     * Returns the version number as an integer array, in the form
     * [major, minor, micro] or [major, minor, micro, preview].
     *
     * This is useful to initialize an instance of
     * {@code org.apache.tools.ant.util.DeweyDecimal} using a
     * {@link FullRevision}.
     *
     * @param includePreview If true the output will contain 4 fields
     *  to include the preview number (even if 0.) If false the output
     *  will contain only 3 fields (major, minor and micro.)
     * @return A new int array, never null, with either 3 or 4 fields.
     */
    public int[] toIntArray(boolean includePreview) {
        int size = includePreview ? 4 : 3;
        int[] result = new int[size];
        result[0] = mMajor;
        result[1] = mMinor;
        result[2] = mMicro;
        if (result.length > 3) {
            result[3] = mPreview;
        }
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + mMajor;
        result = prime * result + mMinor;
        result = prime * result + mMicro;
        result = prime * result + mPreview;
        return result;
    }

    @Override
    public boolean equals(Object rhs) {
        if (this == rhs) {
            return true;
        }
        if (rhs == null) {
            return false;
        }
        if (!(rhs instanceof FullRevision)) {
            return false;
        }
        FullRevision other = (FullRevision) rhs;
        if (mMajor != other.mMajor) {
            return false;
        }
        if (mMinor != other.mMinor) {
            return false;
        }
        if (mMicro != other.mMicro) {
            return false;
        }
        if (mPreview != other.mPreview) {
            return false;
        }
        return true;
    }

    /**
     * Trivial comparison of a version, e.g 17.1.2 < 18.0.0.
     *
     * Note that preview/release candidate are released before their final version,
     * so "18.0.0 rc1" comes below "18.0.0". The best way to think of it as if the
     * lack of preview number was "+inf":
     * "18.1.2 rc5" => "18.1.2.5" so its less than "18.1.2.+INF" but more than "18.1.1.0"
     * and more than "18.1.2.4"
     *
     * @param rhs The right-hand side {@link FullRevision} to compare with.
     * @return &lt;0 if lhs &lt; rhs; 0 if lhs==rhs; &gt;0 if lhs &gt; rhs.
     */
    @Override
    public int compareTo(FullRevision rhs) {
        return compareTo(rhs, PreviewComparison.COMPARE_NUMBER);
    }

    /**
     * Trivial comparison of a version, e.g 17.1.2 < 18.0.0.
     *
     * Note that preview/release candidate are released before their final version,
     * so "18.0.0 rc1" comes below "18.0.0". The best way to think of it as if the
     * lack of preview number was "+inf":
     * "18.1.2 rc5" => "18.1.2.5" so its less than "18.1.2.+INF" but more than "18.1.1.0"
     * and more than "18.1.2.4"
     *
     * @param rhs The right-hand side {@link FullRevision} to compare with.
     * @param comparePreview How to compare the preview value.
     * @return &lt;0 if lhs &lt; rhs; 0 if lhs==rhs; &gt;0 if lhs &gt; rhs.
     */
    public int compareTo(FullRevision rhs, PreviewComparison comparePreview) {
        int delta = mMajor - rhs.mMajor;
        if (delta != 0) {
            return delta;
        }

        delta = mMinor - rhs.mMinor;
        if (delta != 0) {
            return delta;
        }

        delta = mMicro - rhs.mMicro;
        if (delta != 0) {
            return delta;
        }

        int p1, p2;
        switch (comparePreview) {
        case IGNORE:
            // Nothing to compare.
            break;

        case COMPARE_NUMBER:
            p1 =     mPreview == NOT_A_PREVIEW ? Integer.MAX_VALUE :     mPreview;
            p2 = rhs.mPreview == NOT_A_PREVIEW ? Integer.MAX_VALUE : rhs.mPreview;
            delta = p1 - p2;
            break;

        case COMPARE_TYPE:
            p1 =     mPreview == NOT_A_PREVIEW ? 1 : 0;
            p2 = rhs.mPreview == NOT_A_PREVIEW ? 1 : 0;
            delta = p1 - p2;
            break;
        }
        return delta;
    }

    /** Indicates how to compare the preview field in
     *  {@link FullRevision#compareTo(FullRevision, PreviewComparison)} */
    public enum PreviewComparison {
        /** Both revisions must have exactly the same preview number. */
        COMPARE_NUMBER,
        /** Both revisions must have the same preview type (both must be previews
         *  or both must not be previews, but the actual number is irrelevant.)
         *  This is the most typical choice used to find updates of the same type. */
        COMPARE_TYPE,
        /** The preview field is ignored and not used in the comparison. */
        IGNORE
    }


}
