package com.duy.compile.diagnostic;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import com.duy.editor.R;

import java.io.File;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Created by duy on 19/07/2017.
 */

public class SpanUtil {

    public static Spannable createSrcSpan(Resources resources, @NonNull Diagnostic diagnostic) {
        if (!(diagnostic.getSource() instanceof JavaFileObject)) {
            return new SpannableString(diagnostic.getSource().toString());
        }
        try {
            JavaFileObject source = (JavaFileObject) diagnostic.getSource();
            File file = new File(source.getName());
            String name = file.getName();
            String line = diagnostic.getLineNumber() + ":" + diagnostic.getColumnNumber();
            SpannableString span = new SpannableString(name + ":" + line);
            span.setSpan(new ForegroundColorSpan(resources.getColor(R.color.dark_color_diagnostic_file)),
                    0, span.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return span;
        } catch (Exception e) {

        }
        return new SpannableString(diagnostic.getSource().toString());
    }
}
