package com.duy.ide.javaide.editor.autocomplete;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class JavaAutoComplete implements TextWatcher {
    private EditText editText;

    public JavaAutoComplete(EditText editText) {
        this.editText = editText;
    }

    public void start() {
        editText.removeTextChangedListener(this);
        editText.addTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
