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
import com.android.ide.common.blame.parser.PatternAwareOutputParser;
import com.android.utils.ILogger;

import java.util.List;

/**
 * Parses AAPT output.
 */
public class AaptOutputParser implements PatternAwareOutputParser {

    private static final AbstractAaptOutputParser[] PARSERS = {
            new SkippingHiddenFileParser(),
            new Error1Parser(),
            // this needs to be tested before ERROR_2 since they both start with 'ERROR:'
            new Error6Parser(),
            new Error2Parser(),
            new Error3Parser(),
            new Error4Parser(),
            new Warning1Parser(),
            new Error5Parser(),
            new Error7Parser(),
            new Error8Parser(),
            new SkippingWarning2Parser(),
            new SkippingWarning1Parser(),
            new BadXmlBlockParser()
    };

    @Override
    public boolean parse(@NonNull String line, @NonNull OutputLineReader reader, @NonNull List<Message> messages, @NonNull ILogger logger) {
        for (AbstractAaptOutputParser parser : PARSERS) {
            try {
                if (parser.parse(line, reader, messages, logger)) {
                    return true;
                }
            } catch (ParsingFailedException e) {
                // If there's an exception, it means a parser didn't like the input, so just ignore and let other parsers have a crack at it.
            }
        }
        return false;
    }
}
