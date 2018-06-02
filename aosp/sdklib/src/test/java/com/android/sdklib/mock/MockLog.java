/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.sdklib.mock;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.utils.ILogger;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

/**
 * An instance of {@link ILogger} that captures all messages to an internal list.
 * Messages can be retrieved later using {@link #toString()}.
 * Useful for unit-tests.
 */
public class MockLog implements ILogger {
    private ArrayList<String> mMessages = new ArrayList<String>();

    private void add(String code, String format, Object... args) {
        Formatter formatter = new Formatter();
        mMessages.add(formatter.format(code + format, args).toString());
        formatter.close();
    }

    @Override
    public void warning(@NonNull String format, Object... args) {
        add("W ", format, args);
    }

    @Override
    public void info(@NonNull String format, Object... args) {
        add("P ", format, args);
    }

    @Override
    public void verbose(@NonNull String format, Object... args) {
        add("V ", format, args);
    }

    @Override
    public void error(@Nullable Throwable t, @Nullable String format, Object... args) {
        if (t != null) {
            add("T", "%s", t.toString());
        }
        add("E ", format, args);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // Each line starts with [WPVE] + space + actual content.
        // When writing the types, collapse the W/P/V/E qualifiers and only specify one per line.
        char lastType = 0;
        for (String s : mMessages) {
            if (s.isEmpty()) {
                continue;
            }
            char type = s.charAt(0);
            if (type != lastType) {
                sb.append(s);
                lastType = type;
            } else if (s.length() > 2) {
                sb.append(s.substring(2));
            }
            if (s.endsWith("\n")) {
                lastType = 0;
            }
        }
        return sb.toString();
    }

    @NonNull
    public List<String> getMessages() {
        return mMessages;
    }

    public void clear() {
        mMessages.clear();
    }
}
