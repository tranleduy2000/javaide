package com.duy.ide.javaide.editor.format;

import android.content.Context;
import android.support.annotation.Nullable;

import com.duy.ide.code.api.CodeFormatter;
import com.duy.ide.java.setting.AppSetting;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.JavaFormatterOptions;

public class JavaFormatter implements CodeFormatter {
    private Context context;

    JavaFormatter(Context context) {
        this.context = context;
    }

    @Nullable
    @Override
    public CharSequence format(CharSequence input) {
        try {
            AppSetting setting = new AppSetting(context);
            JavaFormatterOptions.Builder builder = JavaFormatterOptions.builder();
            builder.style(setting.getFormatType() == 0
                    ? JavaFormatterOptions.Style.GOOGLE : JavaFormatterOptions.Style.AOSP);
            return new Formatter(builder.build()).formatSource(input.toString());
        } catch (Exception e) {
            return null;
        }
    }
}
