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

package com.android.sdklib.repository;

import com.android.annotations.NonNull;

/**
 * A {@link FullRevision} which distinguishes between x and x.0, x.0.0, x.y.0, etc; it basically
 * keeps track of the precision of the revision string.
 * <p>
 * This is vital when referencing Gradle artifact numbers,
 * since versions x.y.0 and version x.y are not the same.
 */
public class PreciseRevision extends FullRevision {
    private final int mPrecision;

    /**
     * Parses a string of format "major.minor.micro rcPreview" and returns
     * a new {@link com.android.sdklib.repository.PreciseRevision} for it.
     *
     * All the fields except major are optional.
     * <p/>
     * @param revision A non-null revision to parse.
     * @return A new non-null {@link com.android.sdklib.repository.PreciseRevision}.
     * @throws NumberFormatException if the parsing failed.
     */
    @NonNull
    public static PreciseRevision parseRevision(@NonNull String revision)
            throws NumberFormatException {
        return (PreciseRevision) parseRevisionImpl(revision, true /*supportMinorMicro*/,
                true /*supportPreview*/, true /*keepPrevision*/);
    }

    public PreciseRevision(int major) {
        this(major, IMPLICIT_MINOR_REV, IMPLICIT_MICRO_REV, NOT_A_PREVIEW, PRECISION_MAJOR,
            DEFAULT_SEPARATOR);
    }

    public PreciseRevision(int major, int minor) {
        this(major, minor, IMPLICIT_MICRO_REV, NOT_A_PREVIEW, PRECISION_MINOR, DEFAULT_SEPARATOR);
    }

    public PreciseRevision(int major, int minor, int micro) {
        this(major, minor, micro, NOT_A_PREVIEW, PRECISION_MICRO, DEFAULT_SEPARATOR);
    }

    public PreciseRevision(int major, int minor, int micro, int preview) {
      this(major, minor, micro, preview, PRECISION_PREVIEW, DEFAULT_SEPARATOR);
    }

    PreciseRevision(int major, int minor, int micro, int preview, int precision,
            String separator) {
        this(major, minor, micro, preview, precision, separator, PreviewType.RC);
    }

    PreciseRevision(int major, int minor, int micro, int preview, int precision,
            String separator, FullRevision.PreviewType previewType) {
        super(major, minor, micro, previewType, preview, separator);
        mPrecision = precision;
    }

    /**
     * Returns the version in a fixed format major.minor.micro
     * with an optional "rc preview#". For example it would
     * return "18.0.0", "18.1.0" or "18.1.2 rc5".
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getMajor());

        if (mPrecision >= PRECISION_MINOR) {
            sb.append('.').append(getMinor());
            if (mPrecision >= PRECISION_MICRO) {
                sb.append('.').append(getMicro());
                if (mPrecision >= PRECISION_PREVIEW && isPreview()) {
                    sb.append(getSeparator()).append("rc").append(getPreview());
                }
            }
        }

        return sb.toString();
    }

    @Override
    public String toShortString() {
        return toString();
    }

    @Override
    public int[] toIntArray(boolean includePreview) {
        int[] result;
        if (mPrecision >= PRECISION_PREVIEW) {
            if (includePreview) {
                result = new int[mPrecision];
                result[3] = getPreview();
            } else {
                result = new int[mPrecision - 1];
            }
        } else {
            result = new int[mPrecision];
        }
        result[0] = getMajor();
        if (mPrecision >= PRECISION_MINOR) {
            result[1] = getMinor();
            if (mPrecision >= PRECISION_MICRO) {
                result[2] = getMicro();
            }
        }

        return result;
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + mPrecision;
    }

    @Override
    public boolean equals(Object rhs) {
        boolean equals = super.equals(rhs);
        if (equals) {
            if (!(rhs instanceof PreciseRevision)) {
                return false;
            }
            PreciseRevision other = (PreciseRevision) rhs;
            return mPrecision == other.mPrecision;
        }
        return false;
    }

    public int compareTo(PreciseRevision rhs, PreviewComparison comparePreview) {
        int delta = super.compareTo(rhs, comparePreview);
        if (delta == 0) {
            return mPrecision - rhs.mPrecision;
        }
        return delta;
    }
}
