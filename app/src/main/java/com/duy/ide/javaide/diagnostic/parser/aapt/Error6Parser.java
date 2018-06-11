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
import com.google.common.base.Strings;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Error6Parser extends AbstractAaptOutputParser {

    /**
     * 4-line aapt error.
     * <pre>
     * ERROR: 9-path image &lt;path&gt; malformed
     * </pre>
     * <p/> Line 2 and 3 are taken as-is while line 4 is ignored. It repeats with
     * <pre>
     * ERROR: failure processing &lt;path&gt;
     * </pre>
     */
    private static final Pattern MSG_PATTERN = Pattern
            .compile("^ERROR:\\s+9-patch\\s+image\\s+(.+)\\s+malformed\\.$");

    @Override
    public boolean parse(@NonNull String line, @NonNull OutputLineReader reader, @NonNull List<Message> messages, @NonNull ILogger logger)
            throws ParsingFailedException {
        Matcher m = MSG_PATTERN.matcher(line);
        if (!m.matches()) {
            return false;
        }
        String sourcePath = m.group(1);
        String msgText = line; // default message is the line in case we don't find anything else
        if (reader.hasNextLine()) {
            msgText = Strings.nullToEmpty(reader.readLine()).trim();
            if (reader.hasNextLine()) {
                msgText = msgText + " - " + Strings.nullToEmpty(reader.readLine()).trim();
                // skip the next line
                reader.skipNextLine();
            }
        }
        Message msg = createMessage(Message.Kind.ERROR, msgText, sourcePath,
                null, "", logger);
        messages.add(msg);
        return true;
    }
}
