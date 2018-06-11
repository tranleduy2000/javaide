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
import com.duy.ide.diagnostic.util.OutputLineReader;
import com.duy.ide.logging.ILogger;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class BadXmlBlockParser extends AbstractAaptOutputParser {

    private static final Pattern MSG_PATTERN = Pattern
            .compile("W/ResourceType\\(.*\\): Bad XML block: no root element node found");

    @Override
    public boolean parse(@NonNull String line, @NonNull OutputLineReader reader, @NonNull List<Message> messages, @NonNull ILogger logger)
            throws ParsingFailedException {
        Matcher m = MSG_PATTERN.matcher(line);
        if (!m.matches()) {
            return false;
        }
        // W/ResourceType(12345): Bad XML block: no root element node found.
        // Sadly there's NO filename reference; this error typically describes the error *after* this line.
        if (reader.getLineCount() == 1) {
            // This is the only error message: dump to console and quit.
            throw new ParsingFailedException();
        }
        // Continue: the real culprit is displayed next and should get a marker.
        return true;
    }
}
