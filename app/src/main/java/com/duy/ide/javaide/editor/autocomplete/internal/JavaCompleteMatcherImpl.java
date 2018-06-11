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
