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

package com.android.ide.common.blame.parser;

import com.android.annotations.NonNull;
import com.android.ide.common.blame.Message;
import com.android.ide.common.blame.SourceFilePosition;
import com.android.ide.common.blame.parser.util.OutputLineReader;
import com.android.utils.ILogger;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

public class ToolOutputParser {

    @NonNull
    private final List<PatternAwareOutputParser> mParsers;

    @NonNull
    private final ILogger mLogger;

    public ToolOutputParser(@NonNull Iterable<PatternAwareOutputParser> parsers, @NonNull ILogger logger) {
        mParsers = ImmutableList.copyOf(parsers);
        mLogger = logger;
    }

    public ToolOutputParser(@NonNull PatternAwareOutputParser [] parsers, @NonNull ILogger logger) {
        mParsers = ImmutableList.copyOf(parsers);
        mLogger = logger;
    }

    public ToolOutputParser(@NonNull PatternAwareOutputParser parser, @NonNull ILogger logger) {
        mParsers = ImmutableList.of(parser);
        mLogger = logger;
    }

    public List<Message> parseToolOutput(@NonNull String output) {
        OutputLineReader outputReader = new OutputLineReader(output);

        if (outputReader.getLineCount() == 0) {
            return Collections.emptyList();
        }

        List<Message> messages = Lists.newArrayList();
        String line;
        while ((line = outputReader.readLine()) != null) {
            if (line.isEmpty()) {
                continue;
            }
            boolean handled = false;
            for (PatternAwareOutputParser parser : mParsers) {
                try {
                    if (parser.parse(line, outputReader, messages, mLogger)) {
                        handled = true;
                        break;
                    }
                }
                catch (ParsingFailedException e) {
                    return Collections.emptyList();
                }
            }
            if (handled) {
                int messageCount = messages.size();
                if (messageCount > 0) {
                    Message last = messages.get(messageCount - 1);
                    if (last.getText().contains("Build cancelled")) {
                        // Build was cancelled, just quit. Extra messages are just confusing noise.
                        break;
                    }
                }
            }
            else {
                // If none of the standard parsers recogni ze the input, include it as info such
                // that users don't miss potentially vital output such as gradle plugin exceptions.
                // If there is predictable useless input we don't want to appear here, add a custom
                // parser to digest it.
                messages.add(new Message(Message.Kind.SIMPLE, line, SourceFilePosition.UNKNOWN));
            }
        }
        return messages;
    }
}
