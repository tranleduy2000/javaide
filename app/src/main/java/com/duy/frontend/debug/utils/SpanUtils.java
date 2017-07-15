/*
 *  Copyright (c) 2017 Tran Le Duy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duy.frontend.debug.utils;

import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import com.duy.pascal.interperter.ast.variablecontext.ContainsVariables;
import com.duy.pascal.interperter.declaration.lang.types.Type;
import com.duy.pascal.interperter.declaration.lang.types.set.ArrayType;
import com.duy.pascal.interperter.declaration.lang.value.VariableDeclaration;
import com.duy.frontend.themefont.themes.database.CodeTheme;

import java.util.List;

/**
 * Created by Duy on 09-Jun-17.
 */

public class SpanUtils {

    private static final String TAG = "SpanUtils";
    private CodeTheme codeTheme;
    private int maxLengthArray = 10;

    public SpanUtils(CodeTheme codeTheme) {

        this.codeTheme = codeTheme;
    }

    public Spannable generateNameSpan(String name) {
        SpannableString text = new SpannableString(name);
        text.setSpan(new ForegroundColorSpan(codeTheme.getKeywordColor()), 0, text.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return text;
    }

    public Spannable generateTypeSpan(@Nullable Type declaredType, boolean isEnd) {
        if (declaredType == null) return new SpannableString("null");
        SpannableStringBuilder spannableString;
        if (declaredType instanceof ArrayType) {
            ArrayType arrayType = (ArrayType) declaredType;
            if (arrayType.getBound() == null) { //dynamic array, non bound
                Type elementType = arrayType.getElementType();
                spannableString = new SpannableStringBuilder()
                        .append("[]")
                        .append(generateTypeSpan(elementType, false));
            } else {//static array
                spannableString = new SpannableStringBuilder()
                        .append("[").append(arrayType.getBound().toString()).append("]")
                        .append(generateTypeSpan(arrayType.getElementType(), false));
            }
        } else {
            spannableString = new SpannableStringBuilder(declaredType.toString());
        }
        if (isEnd) {
            spannableString = new SpannableStringBuilder("{").append(spannableString).append("}");
        }
        spannableString.setSpan(new ForegroundColorSpan(codeTheme.getCommentColor()), 0,
                spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    public Spannable generateValueSpan(@Nullable Object value) {
        if (value != null) {
            Spannable spannableString = new SpannableString("");
            if (value instanceof Number) { //number
                spannableString = new SpannableString(value.toString());
                spannableString.setSpan(new ForegroundColorSpan(codeTheme.getNumberColor()), 0,
                        spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (value instanceof String || value instanceof Character ||
                    value instanceof StringBuilder) { //string or char
                spannableString = new SpannableString("'" + value.toString() + "'");
                spannableString.setSpan(new ForegroundColorSpan(codeTheme.getStringColor()), 0,
                        spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (value instanceof Object[]) { //array
                spannableString = getSpanArray((Object[]) value, maxLengthArray);
            } else if (value instanceof List) { //set, enum
                spannableString = new SpannableString(listToString((List) value, 10));
            } else if (value instanceof ContainsVariables) { //record
                spannableString = new SpannableString(value.toString());
            }
            return spannableString;
        }
        return new SpannableString("null");
    }

    private SpannableString getSpanArray(Object[] array, int maxLength) {
        if (array == null || array.length == 0) {
            return new SpannableString("[]");
        }
        if (maxLength == -1) maxLength = array.length;
        if (array[0] instanceof Object[]) {
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            spannableStringBuilder.append("\n").append("[");
            if (array.length <= maxLength) {
                for (int i = 0; i < array.length; i++) {
                    spannableStringBuilder.append(getSpanArray((Object[]) array[i], maxLength));
                    if (i == array.length - 1) {
                        return new SpannableString(spannableStringBuilder.append("]"));
                    }
                    spannableStringBuilder.append(", ").append("\n");
                }
            } else {
                for (int i = 0; i < maxLength; i++) {
                    spannableStringBuilder.append(getSpanArray((Object[]) array[i], maxLength));
                    if (i == maxLength - 1) {
                        return new SpannableString(spannableStringBuilder.append("...]"));
                    }
                    spannableStringBuilder.append(", ").append("\n");
                }
                spannableStringBuilder.append("...");
            }
            return new SpannableString(spannableStringBuilder);
        } else {
            if (array.length <= maxLength) {
                SpannableStringBuilder b = new SpannableStringBuilder();
                b.append('[');
                for (int i = 0; i < array.length; i++) {
                    b.append(generateValueSpan(array[i]));
                    if (i == array.length - 1)
                        return new SpannableString(b.append("]"));
                    b.append(", ");
                }
            } else {
                SpannableStringBuilder b = new SpannableStringBuilder();
                b.append('[');
                for (int i = 0; i < maxLength; i++) {
                    b.append(generateValueSpan(array[i]));
                    if (i == maxLength - 1)
                        return new SpannableString(b.append("...]"));
                    b.append(", ");
                }
            }
        }
        return null;
    }

    public String listToString(List list, int maxSize) {
        if (list == null) return "";
        if (maxSize == -1) maxSize = list.size();
        if (list.size() <= maxSize) {
            StringBuilder b = new StringBuilder();
            b.append('[');
            for (int i = 0; i < list.size(); i++) {
                b.append(generateValueSpan(list.get(i)));
                if (i == maxSize - 1)
                    return b.append("]").toString();
                b.append(", ");
            }
            return b.toString();
        } else {
            StringBuilder b = new StringBuilder();
            b.append('[');
            for (int i = 0; i < maxSize; i++) {
                b.append(generateValueSpan(list.get(i)));
                if (i == maxSize - 1)
                    return b.append("...]").toString();
                b.append(", ");
            }
            return b.toString();
        }
    }

    public SpannableStringBuilder createVarSpan(VariableDeclaration var) {

        SpannableStringBuilder text = new SpannableStringBuilder();
        text.append(generateNameSpan(var.getName()));
        text.append(generateTypeSpan(var.getType(), true));
        text.append(" = ");
        text.append(generateValueSpan(var.getInitialValue()));
        return text;
    }

    public void setMaxLengthArray(int maxLengthArray) {
        this.maxLengthArray = maxLengthArray;
    }
}
