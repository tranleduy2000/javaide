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

package com.duy.ide.editor.code.highlight.java;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.text.Editable;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;

import com.duy.ide.editor.code.highlight.Highlighter;
import com.duy.ide.themefont.themes.database.CodeTheme;

import java.util.ArrayList;
import java.util.regex.Matcher;

import static com.duy.ide.java.autocomplete.Patterns.JAVA_COMMENTS;

/**
 * Created by Duy on 18-Jun-17.
 */

public class JavaCommentHighlighter implements Highlighter {
    private ArrayList<Pair<Integer, Integer>> mCommentRegion = new ArrayList<>();
    private CodeTheme codeTheme;

    public JavaCommentHighlighter(CodeTheme codeTheme) {
        this.codeTheme = codeTheme;
    }

    @Override
    public void highlight(@NonNull Editable allText, @NonNull CharSequence textToHighlight, int start) {
        mCommentRegion.clear();
        for (Matcher m = JAVA_COMMENTS.matcher(textToHighlight); m.find(); ) {
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
