package com.duy.ide.javaide.editor.autocomplete.api;

import android.widget.EditText;

import java.util.ArrayList;

public interface IAutoCompleteProvider {
    ArrayList<SuggestItem> getSuggestions(EditText editor);
}
