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

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.duy.ide.diagnostic.model.Message;
import com.duy.ide.diagnostic.parser.ParsingFailedException;
import com.duy.ide.diagnostic.util.OutputLineReader;
import com.duy.ide.logging.ILogger;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Error5Parser extends AbstractAaptOutputParser {

    /**
     * Single-line aapt error.
     * <pre>
     * &lt;path&gt;:&lt;line&gt;: error: Error: &lt;error&gt;
     * &lt;path&gt;:&lt;line&gt;: error: &lt;error&gt;
     * &lt;path&gt;:&lt;line&gt;: &lt;error&gt;
     * </pre>
     */
    private static final List<Pattern> MSG_PATTERNS = ImmutableList.of(
            Pattern.compile("^(.+?):(\\d+): error: Error:\\s+(.+)$"),
            Pattern.compile("^(.+?):(\\d+): error:\\s+(.+)$"),
            Pattern.compile("^(.+?):(\\d+):\\s+(.+)$")
    );

    @Override
    public boolean parse(@NonNull String line, @NonNull OutputLineReader reader, @NonNull List<Message> messages, @NonNull ILogger logger)
            throws ParsingFailedException {
        for (Pattern pattern : MSG_PATTERNS) {
            Matcher m = pattern.matcher(line);
            if (m.matches()) {
                String sourcePath = m.group(1);
                String lineNumber = m.group(2);
                String msgText = m.group(3);
                Message.Kind kind = Message.Kind.ERROR;
                if (msgText.startsWith("warning: ")) {
                    // NDK warning also matches this regexp
                    kind = Message.Kind.WARNING;
                }
                if (sourcePath.endsWith(SdkConstants.DOT_JAVA)) {
                    return false;
                }
                Message msg = createMessage(kind, msgText, sourcePath, lineNumber, "", logger);
                messages.add(msg);
                return true;
            }
        }
        return false;
    }
}
