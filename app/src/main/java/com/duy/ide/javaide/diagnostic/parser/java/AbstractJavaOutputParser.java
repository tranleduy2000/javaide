package com.duy.ide.javaide.diagnostic.parser.java;

import com.duy.ide.diagnostic.model.SourcePosition;
import com.duy.ide.diagnostic.parser.ParsingFailedException;
import com.duy.ide.diagnostic.parser.PatternAwareOutputParser;


abstract class AbstractJavaOutputParser implements PatternAwareOutputParser {

    SourcePosition parseLineNumber(String lineNumberAsText) throws ParsingFailedException {
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
