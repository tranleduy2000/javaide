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
import com.android.annotations.Nullable;
import com.android.ide.common.blame.parser.ToolOutputParser;
import com.android.ide.common.process.BaseProcessOutputHandler;
import com.android.ide.common.process.ProcessException;
import com.android.ide.common.process.ProcessOutput;
import com.android.utils.ILogger;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * A ProcessOutputHandler that runs ToolOutputParsers over the process output and emits Messages.
 */
public class ParsingProcessOutputHandler extends BaseProcessOutputHandler {

    @NonNull
    private final ToolOutputParser mErrorToolOutputParser;

    @NonNull
    private final ToolOutputParser mStdoutToolOutputParser;

    @NonNull
    private final List<MessageReceiver> mMessageReceivers;

    /**
     * Create a ParsingProcessOutputHandler.
     *
     * @param errorToolOutputParser the {@link ToolOutputParser} to use for process output sent
     *                              to stderr,
     * @param stdoutToolOutputParser the ToolOutputParser to use for process output to sent to
     *                               stdout.
     * @param messageReceivers the message receivers to notify for each message,
     */
    public ParsingProcessOutputHandler(
            @NonNull ToolOutputParser errorToolOutputParser,
            @NonNull ToolOutputParser stdoutToolOutputParser,
            @NonNull MessageReceiver... messageReceivers) {
        mErrorToolOutputParser = errorToolOutputParser;
        mStdoutToolOutputParser = stdoutToolOutputParser;
        mMessageReceivers = ImmutableList.copyOf(messageReceivers);
    }

    /**
     * Create a ParsingProcessOutputHandler.
     *
     * @param toolOutputParser the {@link ToolOutputParser} to use for process output sent to
     *                         stderr and stdout,
     * @param messageReceivers the message receivers to notify for each message,
     */
    public ParsingProcessOutputHandler(
            @NonNull ToolOutputParser toolOutputParser,
            @NonNull MessageReceiver... messageReceivers) {
        this(toolOutputParser, toolOutputParser, messageReceivers);
    }

    @Override
    public void handleOutput(@NonNull ProcessOutput processOutput) throws ProcessException {
        if (!(processOutput instanceof BaseProcessOutput)) {
            throw new IllegalArgumentException("processOutput was not created by this handler.");
        }
        BaseProcessOutput impl = (BaseProcessOutput) processOutput;
        String stdout = impl.getStandardOutputAsString();
        if (!stdout.isEmpty()) {
            outputMessages(mStdoutToolOutputParser.parseToolOutput(stdout));
        }
        String stderr = impl.getErrorOutputAsString();
        if (!stderr.isEmpty()) {
            outputMessages(mErrorToolOutputParser.parseToolOutput(stderr));
        }
    }

    private void outputMessages(List<Message> messages) {
        for (Message message: messages) {
            for (MessageReceiver messageReceiver: mMessageReceivers) {
                messageReceiver.receiveMessage(message);
            }
        }
    }
}