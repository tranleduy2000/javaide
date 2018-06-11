/*
 * Copyright (C) 2018 Tran Le Duy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.duy.ide.javaide.editor.autocomplete.internal;

import com.duy.common.DLog;
import com.duy.ide.code.api.SuggestItem;
import com.duy.ide.editor.internal.suggestion.Editor;
import com.duy.ide.javaide.editor.autocomplete.model.JavaSuggestItemImpl;

import java.util.ArrayList;

abstract class JavaCompleteMatcherImpl implements IJavaCompleteMatcher {
    private static final String TAG = "JavaCompleteMatcherImpl";

    protected void setInfo(ArrayList<SuggestItem> members, Editor editor, String incomplete) {
        for (SuggestItem member : members) {
            if (member instanceof JavaSuggestItemImpl) {
                setInfo((JavaSuggestItemImpl) member, editor, incomplete);
            } else {
                if (DLog.DEBUG) DLog.w(TAG, "setInfo: not a java suggestion " + member);
            }
        }
    }

    protected void setInfo(JavaSuggestItemImpl member, Editor editor, String incomplete) {
        member.setEditor(editor);
        member.setIncomplete(incomplete);
    }
}
