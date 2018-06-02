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

package com.android.sdklib.repository;

import com.android.annotations.NonNull;


/**
 * Package multi-part revision number composed of a tuple
 * (major.minor.micro) but without support for any optional preview number.
 *
 *  @see FullRevision
 */
public class NoPreviewRevision extends FullRevision {

    public NoPreviewRevision(int major) {
        this(major, IMPLICIT_MINOR_REV, IMPLICIT_MICRO_REV);
    }

    public NoPreviewRevision(int major, int minor, int micro) {
        super(major, minor, micro, NOT_A_PREVIEW);
    }

    /**
     * Parses a string of format "major.minor.micro" and returns
     * a new {@link NoPreviewRevision} for it. All the fields except major are
     * optional.
     * <p/>
     * The parsing is equivalent to the pseudo-BNF/regexp:
     * <pre>
     *   Major/Minor/Micro/Preview := [0-9]+
     *   Revision := Major ('.' Minor ('.' Micro)? )? \s*
     * </pre>
     *
     * @param revision A non-null revision to parse.
     * @return A new non-null {@link NoPreviewRevision}.
     * @throws NumberFormatException if the parsing failed.
     */
    @NonNull
    public static NoPreviewRevision parseRevision(@NonNull String revision)
            throws NumberFormatException {
        FullRevision r = parseRevisionImpl(
                revision, true /*supportMinorMicro*/, false /*supportPreview*/,
                false /*keepPrecision*/);
        return new NoPreviewRevision(r.getMajor(), r.getMinor(), r.getMicro());
    }
}
