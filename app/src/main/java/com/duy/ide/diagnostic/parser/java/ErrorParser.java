package com.duy.ide.diagnostic.parser.java;

import com.android.ide.common.blame.Message;
import com.android.ide.common.blame.parser.ParsingFailedException;
import com.android.ide.common.blame.parser.util.OutputLineReader;
import com.android.utils.ILogger;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ErrorParser extends AbstractJavaOutputParser {

    /**
     * android/SmartImageDownloadActivity.java:37: error: Header cannot be resolved to a type
     */
    private static final Pattern PATTERN = Pattern.compile("(\\S+):([0-9]+): (error:)(.*)");

    @Override
    public boolean parse(String line, OutputLineReader reader, List<Message> messages, ILogger logger) throws ParsingFailedException {
        Matcher matcher = PATTERN.matcher(line);
        if (!matcher.find()) {
            return false;
        }
        String sourcePath = matcher.group(1);
        String text = matcher.group(4);
        String lineNumber = matcher.group(2);
        Message message = createMessage(Message.Kind.ERROR, text, sourcePath, lineNumber, "", logger);
        messages.add(message);
        return true;
    }
}
