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

package com.android.ide.common.res2;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;

import java.io.File;

/** Exception for errors during merging */
public class MergingException extends Exception {
    private String mMessage; // Keeping our own copy since parent prepends exception class name
    private File mFile;
    private int mLine = -1;
    private int mColumn = -1;

    public MergingException(@NonNull String message, @Nullable Throwable cause) {
        super(message, cause);
        mMessage = message;
    }

    public MergingException(@NonNull String message) {
        this(message, null);
    }

    public MergingException(@NonNull Throwable cause) {
        this(cause.getLocalizedMessage(), cause);
    }

    public MergingException setFile(@NonNull File file) {
        mFile = file;
        return this;
    }

    public MergingException setCause(@NonNull Throwable cause) {
        initCause(cause);
        return this;
    }

    public MergingException setLine(int line) {
        mLine = line;
        return this;
    }

    public MergingException setColumn(int column) {
        mColumn = column;
        return this;
    }

    /** Computes the error message to display for this error */
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        String path = null;
        if (mFile != null) {
            path = mFile.getAbsolutePath();
            sb.append(path);
            if (mLine >= 0) {
                sb.append(':');
                sb.append(Integer.toString(mLine));
                if (mColumn >= 0) {
                    sb.append(':');
                    sb.append(Integer.toString(mColumn));
                }
            }
        }

        if (sb.length() > 0) {
            sb.append(':').append(' ');

            // ALWAYS insert the string "Error:" between the path and the message.
            // This is done to make the error messages more simple to detect
            // (since a generic path: message pattern can match a lot of output, basically
            // any labeled output, and we don't want to do file existence checks on any random
            // string to the left of a colon.)
            if (!mMessage.startsWith("Error: ")) {
                sb.append("Error: ");
            }
        } else if (!mMessage.contains("Error: ")) {
            sb.append("Error: ");
        }

        String message = mMessage;

        // If the error message already starts with the path, strip it out.
        // This avoids redundant looking error messages you can end up with
        // like for example for permission denied errors where the error message
        // string itself contains the path as a prefix:
        //    /my/full/path: /my/full/path (Permission denied)
        if (path != null && message.startsWith(path)) {
            int stripStart = path.length();
            if (message.length() > stripStart && message.charAt(stripStart) == ':') {
                stripStart++;
            }
            if (message.length() > stripStart && message.charAt(stripStart) == ' ') {
                stripStart++;
            }
            message = message.substring(stripStart);
        }

        sb.append(message);
        return sb.toString();
    }

    @Override
    public String toString() {
        return getMessage();
    }

    /** @return the source file where the error occurred, if known */
    @Nullable
    public File getFile() {
        return mFile;
    }

    /** @return the 0-based line number, if known, otherwise -1 */
    public int getLine() {
        return mLine;
    }

    /** @return the 0-based column number, if known, otherwise -1 */
    public int getColumn() {
        return mColumn;
    }
}
