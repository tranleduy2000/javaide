/*
 * Copyright (C) 2015 The Android Open Source Project
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
package com.android.ide.common.blame.parser.aapt;

import com.android.annotations.NonNull;
import com.android.ide.common.blame.Message;
import com.android.ide.common.blame.parser.util.OutputLineReader;
import com.android.ide.common.blame.parser.ParsingFailedException;
import com.android.utils.ILogger;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SkippingWarning1Parser extends AbstractAaptOutputParser {

    /**
     * Error message emitted when aapt skips a file because for example it's name is invalid, such
     * as a layout file name which starts with _. <p/> This error message is used by AAPT in Tools
     * 19 and earlier.
     */
    private static final Pattern MSG_PATTERN = Pattern.compile("    \\(skipping (.+) .+ '(.*)'\\)");

    @Override
    public boolean parse(@NonNull String line, @NonNull OutputLineReader reader, @NonNull List<Message> messages, @NonNull ILogger logger)
            throws ParsingFailedException {
        Matcher m = MSG_PATTERN.matcher(line);
        if (!m.matches()) {
            return false;
        }
        String sourcePath = m.group(2);
        // Certain files can safely be skipped without marking the project as having errors.
        // See isHidden() in AaptAssets.cpp:
        String type = m.group(1);
        if (type.equals("backup")         // main.xml~, etc
                || type.equals("hidden")      // .gitignore, etc
                || type.equals("index")) {    // thumbs.db, etc
            return true;
        }
        Message msg = createMessage(Message.Kind.WARNING, line, sourcePath,
                null, "", logger);
        messages.add(msg);
        return true;
    }
}
