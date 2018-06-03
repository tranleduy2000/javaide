package com.duy.ide.diagnostic.parser;

import com.android.ide.common.blame.Message;
import com.android.ide.common.blame.parser.PatternAwareOutputParser;
import com.android.ide.common.blame.parser.util.OutputLineReader;
import com.android.utils.ILogger;

import java.util.List;

public class JavaOutputParser implements PatternAwareOutputParser {
    @Override
    public boolean parse(String line, OutputLineReader reader, List<Message> messages, ILogger logger) {
        return false;
    }
}
