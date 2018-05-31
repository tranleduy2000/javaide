package com.duy.ide.diagnostic;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import com.duy.ide.R;
import com.duy.ide.java.autocomplete.model.ClassDescription;

import java.io.File;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Created by duy on 19/07/2017.
 */

public class SpanUtil {

    public static Spannable createSrcSpan(Resources resources, @NonNull Diagnostic diagnostic) {
        if (diagnostic.getSource() == null) {
            return new SpannableString("Unknown");
        }
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

    public static Spannable formatClass(Context context, ClassDescription item) {
        SpannableString simpleName = new SpannableString(item.getSimpleName());
        SpannableString packageName = new SpannableString("(" + item.getPackageName() + ")");
        simpleName.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context,
                android.R.color.primary_text_dark)), 0, simpleName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        packageName.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context,
                android.R.color.secondary_text_dark)), 0, packageName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return new SpannableStringBuilder().append(simpleName).append(packageName);
    }
}
