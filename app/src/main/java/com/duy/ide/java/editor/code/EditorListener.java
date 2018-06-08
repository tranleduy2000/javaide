package com.duy.ide.java.editor.code;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface EditorListener {
    void saveAs();

    void doFindAndReplace(@NonNull String var1, @NonNull String var2, boolean var3, boolean var4);

    void doFind(@NonNull String var1, boolean var2, boolean var3, boolean var4);

    void saveFile();

    void goToLine(int var1);

    void formatCode();

    void undo();

    void redo();

    void paste();

    void copyAll();

    @Nullable
    String getCode();

    void insert(@NonNull CharSequence var1);
}