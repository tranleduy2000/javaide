/*
 * Copyright (C) 2018 Tran Le Duy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.duy.ide.javaide.diagnostic.parser.java;


import android.support.annotation.NonNull;

import com.duy.ide.diagnostic.model.Message;
import com.duy.ide.diagnostic.parser.ParsingFailedException;
import com.duy.ide.diagnostic.parser.PatternAwareOutputParser;
import com.duy.ide.diagnostic.util.OutputLineReader;
import com.duy.ide.logging.ILogger;

import java.util.List;

public class JavaOutputParser implements PatternAwareOutputParser {
    private static final PatternAwareOutputParser[] PARSERS = {
            new JavaWarningParser(),
            new JavaErrorParser()
    };

    @Override
    public boolean parse(@NonNull String line,
                         @NonNull OutputLineReader reader, @NonNull List<Message> messages,
                         @NonNull ILogger logger) {
        for (PatternAwareOutputParser parser : PARSERS) {
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
