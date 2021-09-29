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
package com.duy.ide.javaide.diagnostic.parser.aapt;

import com.android.annotations.NonNull;
import com.duy.ide.diagnostic.model.Message;
import com.duy.ide.diagnostic.parser.ParsingFailedException;
import com.duy.ide.diagnostic.parser.PatternAwareOutputParser;
import com.duy.ide.diagnostic.util.OutputLineReader;
import com.duy.ide.logging.ILogger;

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
        for (AbstractAaptOutputParser parser : PARSERS)
            try {
                if (parser.parse(line, reader, messages, logger)) {
                    return true;
                }
            } catch (ParsingFailedException e) {
                // If there's an exception, it means a parser didn't like the input, so just ignore and let other parsers have a crack at it.
            }
        return false;
    }
}
