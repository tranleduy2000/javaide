package com.duy.ide.java.editor.code;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface EditorListener {

    void saveFile();

    void formatCode();

    void undo();

    void redo();

    @Nullable
    String getCode();

    void insert(@NonNull CharSequence var1);
}