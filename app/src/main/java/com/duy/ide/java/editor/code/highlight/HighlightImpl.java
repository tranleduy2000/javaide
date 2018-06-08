package com.duy.ide.java.editor.code.highlight;

import com.duy.ide.themefont.themes.database.CodeTheme;

/**
 * Created by Duy on 06-Aug-17.
 */

public abstract class HighlightImpl implements Highlighter {
    protected CodeTheme codeTheme;

    public CodeTheme getCodeTheme() {
        return codeTheme;
    }

    @Override
    public void setCodeTheme(CodeTheme codeTheme) {
        this.codeTheme = codeTheme;
    }

    @Override
    public void setErrorRange(long startPosition, long endPosition) {

    }
}
