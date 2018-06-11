package com.duy.ide.javaide.editor.autocomplete.internal;

import com.android.annotations.NonNull;
import com.duy.ide.code.api.SuggestItem;
import com.duy.ide.editor.internal.suggestion.Editor;

import java.util.List;

interface IJavaCompleteMatcher {
    boolean process();

    void getSuggestion(@NonNull Editor editor,
                       @NonNull String incomplete,
                       @NonNull List<SuggestItem> suggestItems);
}
