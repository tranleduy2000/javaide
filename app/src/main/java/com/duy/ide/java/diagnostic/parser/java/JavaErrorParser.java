package com.duy.ide.java.diagnostic.parser.java;

import android.support.annotation.NonNull;

import com.duy.ide.diagnostic.model.Message;
import com.duy.ide.diagnostic.model.SourceFile;
import com.duy.ide.diagnostic.model.SourceFilePosition;
import com.duy.ide.diagnostic.parser.ParsingFailedException;
import com.duy.ide.diagnostic.util.OutputLineReader;
import com.duy.ide.logging.ILogger;

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
                    new SourceFilePosition(new SourceFile(sourcePath), parseLineNumber(lineNumber)));
            messages.add(message);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
