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
import com.android.ide.common.blame.parser.util.OutputLineReader;
import com.android.utils.ILogger;

import java.util.List;

/**
 * Parses the build output. Implementations are specialized in particular output patterns.
 */
public interface PatternAwareOutputParser {

    /**
     * Parses the given output line.
     *
     * @param line     the line to parse.
     * @param reader   passed in case this parser needs to parse more lines in order to create a
     *                 {@code Message}.
     * @param messages stores the messages created during parsing, if any.
     * @return {@code true} if this parser was able to parser the given line, {@code false}
     * otherwise.
     * @throws ParsingFailedException if something goes wrong (e.g. malformed output.)
     */
    boolean parse(@NonNull String line, @NonNull OutputLineReader reader,
            @NonNull List<Message> messages, @NonNull ILogger logger)
            throws ParsingFailedException;
}
