package com.duy.ide.editor.highlight.xml;

import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.text.Editable;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import com.duy.ide.editor.highlight.HighlightImpl;
import com.duy.ide.editor.highlight.java.StringHighlighter;
import com.duy.ide.editor.view.HighlightEditor;
import com.duy.ide.themefont.themes.database.CodeTheme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;

import static com.duy.ide.editor.completion.Patterns.XML_ATTRS;
import static com.duy.ide.editor.completion.Patterns.XML_TAGS;

/**
 * Created by Duy on 06-Aug-17.
 */

public class XmlHighlighter extends HighlightImpl {
    private StringHighlighter stringHighlighter;
    private XmlCommentHighlighter commentHighlighter;

    public XmlHighlighter(HighlightEditor highlightEditor) {
        this.codeTheme = highlightEditor.getCodeTheme();
        this.commentHighlighter = new XmlCommentHighlighter(codeTheme);
        this.stringHighlighter = new StringHighlighter(codeTheme);
    }

    @Override
    public void highlight(@NonNull Editable allText, @NonNull CharSequence textToHighlight, int start) {
        try {
            commentHighlighter.highlight(allText, textToHighlight, start);
            ArrayList<Pair<Integer, Integer>> region = commentHighlighter.getCommentRegion();
            if (region.size() == 0) {
                highlightStringAndOther(allText, textToHighlight, start);
                return;
            }
            Collections.sort(region, new Comparator<Pair<Integer, Integer>>() {
                @Override
                public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
                    return o1.first.compareTo(o2.first);
                }
            });

            int startIndex, endIndex;
            if (region.size() > 0 && start < region.get(0).first - 1) {
                highlightStringAndOther(allText,
                        allText.subSequence(start, region.get(0).first - 1), start);
            }
            for (int i = 0; i < region.size() - 1; i++) {
                startIndex = region.get(i).second + 1;
                endIndex = region.get(i + 1).first - 1;
                if (startIndex < endIndex) {
                    highlightStringAndOther(allText,
                            allText.subSequence(startIndex, endIndex), startIndex);
                }
            }
            if (region.size() > 0) {
                startIndex = region.get(region.size() - 1).second + 1;
                endIndex = start + textToHighlight.length();
                if (startIndex <= endIndex) {
                    highlightStringAndOther(allText,
                            allText.subSequence(startIndex, endIndex), startIndex);
                }
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    @Override
    public void setCodeTheme(CodeTheme codeTheme) {
        super.setCodeTheme(codeTheme);
        commentHighlighter.setCodeTheme(codeTheme);
        stringHighlighter.setCodeTheme(codeTheme);
    }

    @Override
    public void setErrorRange(long startPosition, long endPosition) {

    }

    private void highlightStringAndOther(@NonNull Editable allText,
                                         @NonNull CharSequence textToHighlight, int start) {
        stringHighlighter.highlight(allText, textToHighlight, start);
        ArrayList<Pair<Integer, Integer>> region = stringHighlighter.getStringRegion();
        if (region.size() == 0) {
            highlightOther(allText, textToHighlight, start);
            return;
        }
        Collections.sort(region, new Comparator<Pair<Integer, Integer>>() {
            @Override
            public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
                return o1.first.compareTo(o2.first);
            }
        });

        int startIndex, endIndex;
        if (region.size() > 0 && start < region.get(0).first - 1) {
            highlightOther(allText,
                    allText.subSequence(start, region.get(0).first - 1), start);
        }
        for (int i = 0; i < region.size() - 1; i++) {
            startIndex = region.get(i).second + 1;
            endIndex = region.get(i + 1).first - 1;
            if (startIndex < endIndex) {
                highlightOther(allText,
                        allText.subSequence(startIndex, endIndex), startIndex);
            }
        }
        if (region.size() > 0) {
            startIndex = region.get(region.size() - 1).second + 1;
            endIndex = start + textToHighlight.length();
            if (startIndex <= endIndex) {
                highlightOther(allText,
                        allText.subSequence(startIndex, endIndex), startIndex);
            }
        }
    }

    private void highlightOther(@NonNull Editable allText,
                                @NonNull CharSequence textToHighlight, int start) {
        for (Matcher m = XML_ATTRS.matcher(textToHighlight); m.find(); ) {
            allText.setSpan(new ForegroundColorSpan(codeTheme.getKeywordColor()),
                    start + m.start(),
                    start + m.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            allText.setSpan(new StyleSpan(Typeface.BOLD),
                    start + m.start(),
                    start + m.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        for (Matcher m = XML_TAGS.matcher(textToHighlight); m.find(); ) {
            allText.setSpan(new ForegroundColorSpan(codeTheme.getKeywordColor()),
                    start + m.start(),
                    start + m.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            allText.setSpan(new StyleSpan(Typeface.BOLD),
                    start + m.start(),
                    start + m.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

}