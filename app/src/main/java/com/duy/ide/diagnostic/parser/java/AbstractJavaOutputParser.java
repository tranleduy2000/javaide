package com.duy.ide.diagnostic.parser.java;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.ide.common.blame.Message;
import com.android.ide.common.blame.SourceFilePosition;
import com.android.ide.common.blame.SourcePosition;
import com.android.ide.common.blame.parser.ParsingFailedException;
import com.android.ide.common.blame.parser.PatternAwareOutputParser;
import com.android.utils.ILogger;

import java.io.File;


public abstract class AbstractJavaOutputParser implements PatternAwareOutputParser {


    @NonNull
    Message createMessage(@NonNull Message.Kind kind,
                          @NonNull String text,
                          @Nullable String sourcePath,
                          @Nullable String lineNumberAsText,
                          @NonNull String original,
                          ILogger logger) throws ParsingFailedException {
        File file = null;
        if (sourcePath != null) {
            file = new File(sourcePath);
            if (!file.isFile()) {
                throw new ParsingFailedException();
            }
        }
        SourcePosition errorPosition = parseLineNumber(lineNumberAsText);
        return new Message(kind, text, original, new SourceFilePosition(file, errorPosition));
    }

    private SourcePosition parseLineNumber(String lineNumberAsText) throws ParsingFailedException {
        int lineNumber = -1;
        if (lineNumberAsText != null) {
            try {
                lineNumber = Integer.parseInt(lineNumberAsText);
            } catch (NumberFormatException e) {
                throw new ParsingFailedException();
            }
        }

        return new SourcePosition(lineNumber - 1, -1, -1);
    }
}
