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

package com.android.ide.common.blame;

import com.android.annotations.NonNull;
import com.android.ide.common.blame.parser.ParsingFailedException;
import com.android.ide.common.blame.parser.PatternAwareOutputParser;
import com.android.ide.common.blame.parser.ToolOutputParser;
import com.android.ide.common.blame.parser.util.OutputLineReader;
import com.android.ide.common.process.ProcessException;
import com.android.ide.common.process.ProcessOutput;
import com.android.ide.common.res2.RecordingLogger;
import com.android.utils.ILogger;
import com.google.common.base.Charsets;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ParsingProcessOutputHandlerTest {

    private static ParsingProcessOutputHandler sParsingProcessOutputHandler;

    private static RecordingLogger sLogger;

    private static PatternAwareOutputParser sFakePatternParser;

    private static MessageReceiver sMessageReceiver;

    @BeforeClass
    public static void setUp() throws Exception {
        sLogger = new RecordingLogger();
        sFakePatternParser = new FakePatternAwareOutputParser();

        sMessageReceiver = Mockito.mock(MessageReceiver.class);
        sParsingProcessOutputHandler = new ParsingProcessOutputHandler(
                new ToolOutputParser(sFakePatternParser, sLogger),
                sMessageReceiver);
    }

    @Test
    public void testRewriteMessages() throws IOException, ProcessException {

        String original = "error example\ntwo line error\nnext line\nsomething else";

        sParsingProcessOutputHandler.handleOutput(processOutputFromErrorString(original));

        Mockito.verify(sMessageReceiver).receiveMessage(new Message(
                Message.Kind.ERROR,
                "errorText",
                "originalText",
                new SourceFilePosition(
                        new SourceFile(FakePatternAwareOutputParser.ERROR_EXAMPLE_FILE),
                        new SourcePosition(1, 2, 3, 4, 5, 6)
                )));

        Mockito.verify(sMessageReceiver).receiveMessage(new Message(
                Message.Kind.WARNING,
                "two line warning",
                new SourceFilePosition(
                        new SourceFile(FakePatternAwareOutputParser.TWO_LINE_ERROR_FILE),
                        new SourcePosition(1, 2, -1)
                )));


        Mockito.verify(sMessageReceiver).receiveMessage(new Message(
                Message.Kind.SIMPLE,
                "something else",
                SourceFilePosition.UNKNOWN));
        Mockito.verifyNoMoreInteractions(sMessageReceiver);

        String expected = "AGPBI: {"
                + "\"kind\":\"error\","
                + "\"text\":\"errorText\","
                + "\"sources\":[{"
                + "\"file\":\"" + FakePatternAwareOutputParser.ERROR_EXAMPLE_FILE.getAbsolutePath()
                + "\",\"position\":{\"startLine\":1,\"startColumn\":2,\"startOffset\":3,"
                + "\"endLine\":4,\"endColumn\":5,\"endOffset\":6}}],"
                + "\"original\":\"original_text\"}\n"
                + "AGPBI: {\"kind\":\"warning\","
                + "\"text\":\"two line warning\","
                + "\"sources\":[{\"file\":\"" +
                FakePatternAwareOutputParser.TWO_LINE_ERROR_FILE.getAbsolutePath() + "\","
                + "\"position\":{\"startLine\":1,\"startColumn\":2}}]}\n"
                + "AGPBI: {\"kind\":\"simple\","
                + "\"text\":\"something else\","
                + "\"sources\":[{}]}";
    }

    @Test
    public void parseException() throws IOException, ProcessException {
        String original = "two line error";

        sParsingProcessOutputHandler.handleOutput(processOutputFromErrorString(original));

        Mockito.verifyNoMoreInteractions(sMessageReceiver);
    }

    private static ProcessOutput processOutputFromErrorString(String original) throws IOException {
        ProcessOutput processOutput = sParsingProcessOutputHandler.createOutput();
        processOutput.getErrorOutput().write(original.getBytes(Charsets.UTF_8));
        return processOutput;
    }

    private static class FakePatternAwareOutputParser implements PatternAwareOutputParser {
        static final File ERROR_EXAMPLE_FILE = new File("error/source");
        static final File TWO_LINE_ERROR_FILE = new File("error/source/2");
        @Override
        public boolean parse(@NonNull String line, @NonNull OutputLineReader reader,
                @NonNull List<Message> messages, @NonNull ILogger logger)
                throws ParsingFailedException {
            if (line.equals("two line error")) {
                String nextLine = reader.readLine();
                if ("next line".equals(nextLine)) {
                    messages.add(new Message(
                            Message.Kind.WARNING,
                            "two line warning",
                            "two line warning",
                            new SourceFilePosition(
                                    TWO_LINE_ERROR_FILE,
                                    new SourcePosition(1, 2, -1))));
                } else {
                    throw new ParsingFailedException();
                }
                return true;
            }
            if (line.equals("error example")) {
                messages.add(
                        new Message(
                                Message.Kind.ERROR,
                                "errorText",
                                "original_text",
                                new SourceFilePosition(
                                        ERROR_EXAMPLE_FILE,
                                        new SourcePosition(1, 2, 3, 4, 5, 6))));
                return true;
            }
            return false;
        }
    }
}