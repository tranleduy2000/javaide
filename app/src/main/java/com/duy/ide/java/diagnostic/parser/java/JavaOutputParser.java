package com.duy.ide.java.diagnostic.parser.java;


import android.support.annotation.NonNull;

import com.duy.ide.diagnostic.model.Message;
import com.duy.ide.diagnostic.parser.ParsingFailedException;
import com.duy.ide.diagnostic.parser.PatternAwareOutputParser;
import com.duy.ide.diagnostic.util.OutputLineReader;
import com.duy.ide.logging.ILogger;

import java.util.List;

public class JavaOutputParser implements PatternAwareOutputParser {
    private static final PatternAwareOutputParser[] PARSERS = {
            new JavaWarningParser(),
            new JavaErrorParser()
    };

    @Override
    public boolean parse(@NonNull String line,
                         @NonNull OutputLineReader reader, @NonNull List<Message> messages,
                         @NonNull ILogger logger) {
        for (PatternAwareOutputParser parser : PARSERS) {
            try {
                if (parser.parse(line, reader, messages, logger)) {
                    return true;
                }
            } catch (ParsingFailedException e) {
                // If there's an exception, it means a parser didn't like the input, so just ignore and let other parsers have a crack at it.
            }
        }
        return false;
    }
}
