/*
 *  Copyright (c) 2017 Tran Le Duy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duy.frontend.editor.highlight;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.text.Editable;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;

import com.duy.frontend.editor.view.HighlightEditor;
import com.duy.frontend.themefont.themes.database.CodeTheme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;

import static com.duy.frontend.editor.completion.Patterns.ARGB_FUNCTION;
import static com.duy.frontend.editor.completion.Patterns.BUILTIN_FUNCTIONS;
import static com.duy.frontend.editor.completion.Patterns.KEYWORDS;
import static com.duy.frontend.editor.completion.Patterns.NUMBERS;
import static com.duy.frontend.editor.completion.Patterns.RGB_FUNCTION;
import static com.duy.frontend.editor.completion.Patterns.SYMBOLS;

/**
 * Created by Duy on 18-Jun-17.
 */
public class CodeHighlighter implements Highlighter {
    private static final String TAG = "CodeHighlighter";
    private CodeTheme codeTheme;
    private StringHighlighter stringHighlighter;
    private CommentHighlighter commentHighlighter;

    public CodeHighlighter(HighlightEditor highlightEditor) {
        this.codeTheme = highlightEditor.getCodeTheme();
        this.commentHighlighter = new CommentHighlighter(codeTheme);
        this.stringHighlighter = new StringHighlighter(codeTheme);
    }

    @Override
    public void highlight(@NonNull Editable allText,
                          @NonNull CharSequence textToHighlight, int start) {
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
        this.codeTheme = codeTheme;
        commentHighlighter.setCodeTheme(codeTheme);
        stringHighlighter.setCodeTheme(codeTheme);
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
        //high light number
        for (Matcher m = NUMBERS.matcher(textToHighlight); m.find(); ) {
            allText.setSpan(new ForegroundColorSpan(codeTheme.getNumberColor()),
                    start + m.start(),
                    start + m.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        for (Matcher m = KEYWORDS.matcher(textToHighlight); m.find(); ) {
            allText.setSpan(new ForegroundColorSpan(codeTheme.getKeywordColor()),
                    start + m.start(),
                    start + m.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        for (Matcher m = SYMBOLS.matcher(textToHighlight); m.find(); ) {
            allText.setSpan(new ForegroundColorSpan(codeTheme.getOptColor()),
                    start + m.start(),
                    start + m.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }


        for (Matcher m = RGB_FUNCTION.matcher(textToHighlight); m.find(); ) {
            try {
                int r = Integer.parseInt(m.group(3).trim());
                int g = Integer.parseInt(m.group(5).trim());
                int b = Integer.parseInt(m.group(7).trim());
                int back = Color.rgb(r, g, b);
                int fore = Color.rgb(255 - r, 255 - g, 255 - b);

                allText.setSpan(new BackgroundColorSpan(back),
                        start + m.start(1),
                        start + m.end(1),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                allText.setSpan(new ForegroundColorSpan(fore),
                        start + m.start(1),
                        start + m.end(1),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } catch (Exception e) {

            }
        }
        for (Matcher m = ARGB_FUNCTION.matcher(textToHighlight); m.find(); ) {
            try {
                int r = Integer.parseInt(m.group(3).trim());
                int g = Integer.parseInt(m.group(5).trim());
                int b = Integer.parseInt(m.group(7).trim());
                int back = Color.rgb(r, g, b);
                int fore = Color.rgb(255 - r, 255 - g, 255 - b);

                allText.setSpan(new BackgroundColorSpan(back),
                        start + m.start(1),
                        start + m.end(1),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                allText.setSpan(new ForegroundColorSpan(fore),
                        start + m.start(1),
                        start + m.end(1),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } catch (Exception e) {

            }
        }
    }
}
