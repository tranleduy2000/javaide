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

package com.duy.editor.themefont.themes.database;

import android.content.Context;

import com.duy.editor.R;

/**
 * Created by Duy on 12-Mar-17.
 */

@SuppressWarnings("DefaultFileTemplate")
public class CodeThemeUtils {

    public static int getCodeTheme(Context context, String name) {
        if (name.equals(context.getString(R.string.default_theme))) {
            return R.style.CodeTheme;
        } else if (name.equals(context.getString(R.string.BrightYellow))) {
            return R.style.CodeTheme_BrightYellow;
        } else if (name.equals(context.getString(R.string.DarkGray))) {
            return R.style.CodeTheme_DarkGray;
        } else if (name.equals(context.getString(R.string.EspressoLibre))) {
            return R.style.CodeTheme_EspressoLibre;
        } else if (name.equals(context.getString(R.string.Idel))) {
            return R.style.CodeTheme_Idel;
        } else if (name.equals(context.getString(R.string.KFT2))) {
            return R.style.CodeTheme_KFT2;
        } else if (name.equals(context.getString(R.string.Modnokai_Coffee))) {
            return R.style.CodeTheme_ModnokaiCoffee;
        } else {
            return R.style.CodeTheme;
        }
    }
}
