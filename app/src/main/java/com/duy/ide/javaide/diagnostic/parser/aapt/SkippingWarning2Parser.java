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

class SkippingWarning2Parser extends AbstractAaptOutputParser {

    /**
     * Error message emitted when aapt skips a file because for example it's name is invalid, such
     * as a layout file name which starts with _. <p/> This error message is used by AAPT in Tools
     * 20 and later.
     */
    private static final Pattern MSG_PATTERN = Pattern
            .compile("    \\(skipping .+ '(.+)' due to ANDROID_AAPT_IGNORE pattern '.+'\\)");

    @Override
    public boolean parse(@NonNull String line, @NonNull OutputLineReader reader, @NonNull List<Message> messages, @NonNull ILogger logger)
            throws ParsingFailedException {
        Matcher m = MSG_PATTERN.matcher(line);
        if (!m.matches()) {
            return false;
        }
        String sourcePath = m.group(1);
        if (sourcePath != null && (sourcePath.startsWith(".") || sourcePath.endsWith("~"))) {
            return true;
        }
        Message msg = createMessage(Message.Kind.WARNING, line, sourcePath,
                null, "", logger);
        messages.add(msg);
        return true;
    }
}
