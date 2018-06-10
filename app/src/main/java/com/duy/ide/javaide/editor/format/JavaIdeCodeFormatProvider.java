package com.duy.ide.javaide.editor.format;

import android.content.Context;
import android.support.annotation.Nullable;

import com.duy.ide.code.api.CodeFormatter;
import com.duy.ide.code.format.CodeFormatProviderImpl;
import com.duy.ide.editor.IEditorDelegate;

import java.io.File;

public class JavaIdeCodeFormatProvider extends CodeFormatProviderImpl {

    public JavaIdeCodeFormatProvider(Context context) {
        super(context);
    }

    @Nullable
    @Override
    public CodeFormatter getFormatterForFile(File file, IEditorDelegate delegate) {
        if (file.isFile() && file.getName().endsWith(".java")){
            return new JavaFormatter(getContext());
        }
        return super.getFormatterForFile(file, delegate);
    }
}
