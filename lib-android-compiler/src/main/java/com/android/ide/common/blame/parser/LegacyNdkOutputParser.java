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
import com.android.annotations.Nullable;
import com.android.ide.common.blame.Message;
import com.android.ide.common.blame.SourceFilePosition;
import com.android.ide.common.blame.SourcePosition;
import com.android.ide.common.blame.parser.util.OutputLineReader;
import com.android.utils.ILogger;
import com.android.utils.SdkUtils;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * Parses output from the legacy NDK support.
 */
public class LegacyNdkOutputParser implements PatternAwareOutputParser {

    private static final String FROM = "from";
    private static final String UNKNOWN_MSG_PREFIX1 = "In file included " + FROM;
    private static final String UNKNOWN_MSG_PREFIX2 = "                 " + FROM;

    private static final char COLON = ':';

    @Override
    public boolean parse(@NonNull String line, @NonNull OutputLineReader reader,
            @NonNull List<Message> messages, @NonNull ILogger logger)
            throws ParsingFailedException {
        // Parses unknown message
        if (line.startsWith(UNKNOWN_MSG_PREFIX1) || line.startsWith(UNKNOWN_MSG_PREFIX2)) {
            int fromIndex = line.indexOf(FROM);
            String unknownMsgCause = line.substring(0, fromIndex).trim();
            unknownMsgCause = "(Unknown) " + unknownMsgCause;
            String coordinates = line.substring(fromIndex + FROM.length()).trim();
            if (!coordinates.isEmpty()) {
                int colonIndex1 = line.indexOf(COLON);
                if (colonIndex1 == 1) { // drive letter (Windows)
                    coordinates = coordinates.substring(colonIndex1 + 1);
                }
                if (coordinates.endsWith(",") || coordinates.endsWith(":")) {
                    coordinates = coordinates.substring(0, coordinates.length() - 1);
                }

                List<String> segments = Splitter.on(COLON).splitToList(coordinates);
                if (segments.size() == 3) {
                    String pathname = segments.get(0);
                    File file = new File(pathname);
                    int lineNumber = 0;
                    try {
                        lineNumber = Integer.parseInt(segments.get(1));
                    }
                    catch (NumberFormatException ignore) {
                    }
                    int column = 0;
                    try {
                        column = Integer.parseInt(segments.get(2));
                    }
                    catch (NumberFormatException ignore) {
                    }
                    SourceFilePosition position = new SourceFilePosition(file,
                            new SourcePosition(lineNumber - 1, column - 1, -1));
                    Message message = new Message(Message.Kind.INFO, unknownMsgCause.trim(), position);
                    if (!messages.contains(message)) {
                        // There may be a few duplicate "unknown" messages
                        addMessage(message, messages);
                    }
                }
            }
            return true;
        }

        // Parses unresolved include.
        int colonIndex1 = line.indexOf(COLON);
        if (colonIndex1 == 1) { // drive letter (Windows)
            colonIndex1 = line.indexOf(COLON, colonIndex1 + 1);
        }
        if (colonIndex1 >= 0) { // looks like found something like a file path.
            String part1 = line.substring(0, colonIndex1).trim();

            int colonIndex2 = line.indexOf(COLON, colonIndex1 + 1);
            if (colonIndex2 >= 0) {
                File file = new File(part1);
                if (!file.isFile()) {
                    // the part one is not a file path.
                    return false;
                }
                try {
                    int lineNumber = Integer.parseInt(
                            line.substring(colonIndex1 + 1, colonIndex2).trim()); // 1-based.

                    int colonIndex3 = line.indexOf(COLON, colonIndex2 + 1);
                    if (colonIndex1 >= 0) {
                        int column = Integer.parseInt(
                                line.substring(colonIndex2 + 1, colonIndex3).trim());

                        int colonIndex4 = line.indexOf(COLON, colonIndex3 + 1);
                        if (colonIndex4 >= 0) {
                            Message.Kind kind = Message.Kind.INFO;

                            String severity =
                                    line.substring(colonIndex3 + 1,
                                            colonIndex4).toLowerCase(Locale.getDefault()).trim();
                            if (severity.endsWith("error")) {
                                kind = Message.Kind.ERROR;
                            } else if (severity.endsWith("warning")) {
                                kind = Message.Kind.WARNING;
                            }
                            String text = line.substring(colonIndex4 + 1).trim();
                            List<String> messageList = Lists.newArrayList();
                            messageList.add(text);
                            String prevLine = null;
                            do {
                                String nextLine = reader.readLine();
                                if (nextLine == null) {
                                    return false;
                                }
                                if (nextLine.trim().equals("^")) {
                                    String messageEnd = reader.readLine();

                                    while (isMessageEnd(messageEnd)) {
                                        messageList.add(messageEnd.trim());
                                        messageEnd = reader.readLine();
                                    }

                                    if (messageEnd != null) {
                                        reader.pushBack(messageEnd);
                                    }
                                    break;
                                }
                                if (prevLine != null) {
                                    messageList.add(prevLine);
                                }
                                prevLine = nextLine;
                            } while (true);

                            if (column >= 0) {
                                messageList = convertMessages(messageList);
                                StringBuilder buf = new StringBuilder();
                                for (String m : messageList) {
                                    if (buf.length() > 0) {
                                        buf.append(SdkUtils.getLineSeparator());
                                    }
                                    buf.append(m);
                                }
                                Message msg = new Message(kind, buf.toString(),
                                        new SourceFilePosition(file,
                                                new SourcePosition(lineNumber - 1, column - 1, -1)));
                                if (!messages.contains(msg)) {
                                    addMessage(msg, messages);
                                }
                                return true;
                            }
                        }

                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return false;
    }

    private static void addMessage(@NonNull Message message, @NonNull List<Message> messages) {
        boolean duplicatesPrevious = false;
        int messageCount = messages.size();
        if (messageCount > 0) {
            Message lastMessage = messages.get(messageCount - 1);
            duplicatesPrevious = lastMessage.equals(message);
        }
        if (!duplicatesPrevious) {
            messages.add(message);
        }
    }

    private static boolean isMessageEnd(@Nullable String line) {
        return line != null && !line.isEmpty() && Character.isWhitespace(line.charAt(0));
    }

    @NonNull
    private static List<String> convertMessages(@NonNull List<String> messages) {
        if (messages.size() <= 1) {
            return messages;
        }
        final String line0 = messages.get(0);
        final String line1 = messages.get(1);
        final int colonIndex = line1.indexOf(':');
        if (colonIndex > 0) {
            String part1 = line1.substring(0, colonIndex).trim();
            // jikes
            if ("symbol".equals(part1)) {
                String symbol = line1.substring(colonIndex + 1).trim();
                messages.remove(1);
                if (messages.size() >= 2) {
                    messages.remove(1);
                }
                messages.set(0, line0 + " " + symbol);
            }
        }
        return messages;
    }
}
