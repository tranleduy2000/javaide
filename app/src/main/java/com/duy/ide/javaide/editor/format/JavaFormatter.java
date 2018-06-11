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

package com.duy.ide.javaide.editor.format;

import android.content.Context;
import android.support.annotation.Nullable;

import com.duy.ide.code.api.CodeFormatter;
import com.duy.ide.javaide.setting.AppSetting;
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
