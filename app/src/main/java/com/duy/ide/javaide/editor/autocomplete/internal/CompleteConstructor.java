package com.duy.ide.javaide.editor.autocomplete.internal;

import com.android.annotations.NonNull;
import com.duy.ide.code.api.SuggestItem;
import com.duy.ide.editor.internal.suggestion.Editor;
import com.duy.ide.javaide.editor.autocomplete.dex.JavaDexClassLoader;
import com.duy.ide.javaide.editor.autocomplete.model.ClassDescription;
import com.duy.ide.javaide.editor.autocomplete.model.ConstructorDescription;

import java.util.ArrayList;
import java.util.List;

public class CompleteConstructor implements IJavaCompleteMatcher {

    private final JavaDexClassLoader mClassLoader;

    public CompleteConstructor(JavaDexClassLoader classLoader) {
        mClassLoader = classLoader;
    }

    @Override
    public boolean process() {
        return false;
    }

    @Override
    public void getSuggestion(@NonNull Editor editor,
                              @NonNull String incomplete,
                              @NonNull List<SuggestItem> suggestItems) {
        if (incomplete.isEmpty()) {
            return;
        }
        ArrayList<ClassDescription> classes = mClassLoader.findClassWithPrefix(incomplete);
        for (ClassDescription clazz : classes) {
            ArrayList<ConstructorDescription> constructors = clazz.getConstructors();
            for (ConstructorDescription c : constructors) {
                c.setEditor(editor);
                c.setIncomplete(incomplete);
            }
            suggestItems.addAll(constructors);
        }
    }

}
