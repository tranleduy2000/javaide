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
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Error1Parser extends AbstractAaptOutputParser {

    /**
     * First and second line of dual-line aapt error.
     * <pre>
     * ERROR at line &lt;line&gt;: &lt;error&gt;
     *  (Occurred while parsing &lt;path&gt;)
     * </pre>
     */
    private static final List<Pattern> MSG_PATTERNS = ImmutableList.of(
            Pattern.compile("^ERROR\\s+at\\s+line\\s+(\\d+):\\s+(.*)$"),
            Pattern.compile("^\\s+\\(Occurred while parsing\\s+(.*)\\)$")
    );

    @Override
    public boolean parse(@NonNull String line, @NonNull OutputLineReader reader, @NonNull List<Message> messages, @NonNull ILogger logger)
            throws ParsingFailedException {
        Matcher m = MSG_PATTERNS.get(0).matcher(line);
        if (!m.matches()) {
            return false;
        }
        String lineNumber = m.group(1);
        String msgText = m.group(2);

        m = getNextLineMatcher(reader, MSG_PATTERNS.get(1));
        if (m == null) {
            throw new ParsingFailedException();
        }
        String sourcePath = m.group(1);

        Message msg = createMessage(Message.Kind.ERROR, msgText, sourcePath,
                lineNumber, "", logger);
        messages.add(msg);
        return true;
    }
}
