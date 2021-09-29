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
import com.duy.ide.diagnostic.model.SourceFile;
import com.duy.ide.diagnostic.model.SourceFilePosition;
import com.duy.ide.diagnostic.parser.ParsingFailedException;
import com.duy.ide.diagnostic.util.OutputLineReader;
import com.duy.ide.logging.ILogger;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class JavaErrorParser extends AbstractJavaOutputParser {

    /**
     * android/SmartImageDownloadActivity.java:37: error: Header cannot be resolved to a type
     */
    private static final Pattern PATTERN = Pattern.compile("(\\S+):([0-9]+): (error:)(.*)");

    @Override
    public boolean parse(@NonNull String line, @NonNull OutputLineReader reader,
                         @NonNull List<Message> messages, @NonNull ILogger logger)
            throws ParsingFailedException {
        try {
            Matcher matcher = PATTERN.matcher(line);
            if (!matcher.find()) {
                return false;
            }
            String sourcePath = matcher.group(1);
            String text = matcher.group(4);
            String lineNumber = matcher.group(2);
            Message message = new Message(Message.Kind.ERROR, text,
                    new SourceFilePosition(new SourceFile(new File(sourcePath)), parseLineNumber(lineNumber)));
            messages.add(message);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
