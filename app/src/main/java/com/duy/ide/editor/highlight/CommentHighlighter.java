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

package com.duy.ide.editor.highlight;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.text.Editable;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;

import com.duy.ide.themefont.themes.database.CodeTheme;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Duy on 18-Jun-17.
 */

public class CommentHighlighter implements Highlighter {
    /**
     * match comment, include // { } (* *) comment
     */
    public static final Pattern COMMENTS = Pattern.compile(
            "(//.*)|(/\\*(?:.|[\\n\\r])*?\\*/)"  //splash splash comment
//                   + "|(\\{(?:.|[\\n\\r])*?\\})"  //{ } comment
//                  +  "|((\\(\\*)(?:.|[\\n\\r])*?(\\*\\)))"// (* *) comment
    );
    private ArrayList<Pair<Integer, Integer>> mCommentRegion = new ArrayList<>();
    private CodeTheme codeTheme;

    public CommentHighlighter(CodeTheme codeTheme) {
        this.codeTheme = codeTheme;
    }

    public boolean inComment(int start, int end) {
        //-----------[1 -------------- 3]------
        //--'------------'
        for (Pair<Integer, Integer> pair : mCommentRegion) {
            if (start < pair.first && end > pair.first
                    || start < pair.second && end > pair.second) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void highlight(@NonNull Editable allText, @NonNull CharSequence textToHighlight, int start) {
        mCommentRegion.clear();
        for (Matcher m = COMMENTS.matcher(textToHighlight); m.find(); ) {
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


    public ArrayList<Pair<Integer, Integer>> getCommentRegion() {
        return mCommentRegion;
    }

}
