/*
 * Copyright (C) 2018 Tran Le Duy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.duy.ide.javaide.editor.autocomplete.util;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import com.duy.ide.javaide.editor.autocomplete.model.ClassDescription;

/**
 * Created by duy on 19/07/2017.
 */

public class SpanUtil {


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
