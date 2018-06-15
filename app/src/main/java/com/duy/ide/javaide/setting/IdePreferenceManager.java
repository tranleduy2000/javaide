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

package com.duy.ide.javaide.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.duy.ide.R;
import com.jecelyin.editor.v2.Preferences;


public class IdePreferenceManager {
    private static final String KEY_SET_DEFAULT = "set_default_values";

    public static void setDefaultValues(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(KEY_SET_DEFAULT, false)) {
            return;
        }
        sharedPreferences.edit().putBoolean(KEY_SET_DEFAULT, true).apply();

        PreferenceManager.setDefaultValues(context, R.xml.preference_compiler, false);
        PreferenceManager.setDefaultValues(context, R.xml.preference_editor, false);
        PreferenceManager.setDefaultValues(context, R.xml.preference_logcat, false);

        Preferences preferences = Preferences.getInstance(context);
        //default theme
        preferences.setAppTheme(1);
        preferences.setEditorTheme("allure-contrast.json.properties");

    }
}
