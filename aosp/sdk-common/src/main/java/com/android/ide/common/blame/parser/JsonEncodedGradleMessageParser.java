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
import com.android.ide.common.blame.MessageJsonSerializer;
import com.android.ide.common.blame.parser.util.OutputLineReader;
import com.android.utils.ILogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reconstruct Messages that were parsed by the gradle plugin.
 */
public class JsonEncodedGradleMessageParser implements PatternAwareOutputParser {

    // Android gradle parsed build issue. A prefix that is very unlikely to occur elsewhere to
    // signify the rest of the line should be parsed as a json encoded {@link Message},
    public static final String STDOUT_ERROR_TAG = "AGPBI: ";

    /**
     * The errors are of the form:
     * <pre>AGPBI: {"kind":"ERROR","text":"Nothing"...}</pre>
     */
    private static final Pattern MSG_PATTERN = Pattern.compile("^" + Pattern.quote(
            STDOUT_ERROR_TAG) + "(.*)$");

    @Override
    public boolean parse(@NonNull String line,
            @NonNull OutputLineReader reader,
            @NonNull List<Message> messages,
            @NonNull ILogger logger) throws ParsingFailedException {
        Matcher m = MSG_PATTERN.matcher(line);
        if (!m.matches()) {
            return false;
        }
        String json = m.group(1);
        if (json.trim().isEmpty()) {
            return false;
        }

        GsonBuilder gsonBuilder = new GsonBuilder();
        MessageJsonSerializer.registerTypeAdapters(gsonBuilder);
        Gson gson = gsonBuilder.create();
        try {
            Message msg = gson.fromJson(json, Message.class);
            messages.add(msg);
            return true;
        } catch (JsonParseException e) {
            throw new ParsingFailedException(e);
        }
    }
}
