package com.duy.ide.editor.code.highlight.xml;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.text.Editable;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;

import com.duy.ide.editor.code.highlight.HighlightImpl;
import com.duy.ide.themefont.themes.database.CodeTheme;

import java.util.ArrayList;
import java.util.regex.Matcher;

import static com.duy.ide.java.autocomplete.Patterns.XML_COMMENTS;

/**
 * Created by Duy on 06-Aug-17.
 */

public class XmlCommentHighlighter extends HighlightImpl {
    private ArrayList<Pair<Integer, Integer>> mCommentRegion = new ArrayList<>();
    private CodeTheme codeTheme;

    public XmlCommentHighlighter(CodeTheme codeTheme) {
        this.codeTheme = codeTheme;
    }


    @Override
    public void highlight(@NonNull Editable allText, @NonNull CharSequence textToHighlight, int start) {
        mCommentRegion.clear();
        for (Matcher m = XML_COMMENTS.matcher(textToHighlight); m.find(); ) {
            allText.setSpan(new ForegroundColorSpan(codeTheme.getCommentColor()),
                    start + m.start(),
                    start + m.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mCommentRegion.add(new Pair<>(start + m.start(), start + m.end()));
        }
    }

    @Override
    public void setCodeTheme(CodeTheme codeTheme) {
        this.codeTheme = codeTheme;
    }

    @Override
    public void setErrorRange(long startPosition, long endPosition) {

    }


    public ArrayList<Pair<Integer, Integer>> getCommentRegion() {
        return mCommentRegion;
    }
}
