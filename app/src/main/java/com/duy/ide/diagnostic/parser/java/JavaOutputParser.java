package com.duy.ide.diagnostic.parser.java;

import com.android.ide.common.blame.Message;
import com.android.ide.common.blame.parser.ParsingFailedException;
import com.android.ide.common.blame.parser.PatternAwareOutputParser;
import com.android.ide.common.blame.parser.util.OutputLineReader;
import com.android.utils.ILogger;

import java.util.List;

public class JavaOutputParser implements PatternAwareOutputParser {
    private static final PatternAwareOutputParser[] PARSERS = {
            new WarningParser(),
            new ErrorParser()
    };

    @Override
    public boolean parse(String line, OutputLineReader reader, List<Message> messages, ILogger logger) {
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
