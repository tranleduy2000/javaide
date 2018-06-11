package com.duy.ide.javaide.editor.autocomplete.internal;

import com.duy.ide.code.api.SuggestItem;
import com.duy.ide.editor.internal.suggestion.Editor;

import java.util.List;

public class CompleteStaticAccess implements IJavaCompleteMatcher {
    @Override
    public boolean process() {
        return false;
    }

    @Override
    public void getSuggestion(Editor editor, String incomplete, List<SuggestItem> suggestItems) {

    }
}
