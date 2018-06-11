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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.duy.ide.R;

/**
 * Setting for application
 * <p>
 * Created by Duy on 3/7/2016
 */
public class AppSetting {
    @NonNull
    protected SharedPreferences.Editor editor;
    @NonNull
    protected Context context;
    @NonNull
    private SharedPreferences sharedPreferences;

    @SuppressLint("CommitPrefEdits")
    public AppSetting(@NonNull Context context) {
        this.context = context;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.editor = sharedPreferences.edit();
    }

    public int getInt(String key, int def) {
        try {
            return sharedPreferences.getInt(key, def);
        } catch (Exception e) {
            try {
                return Integer.parseInt(getString(key));
            } catch (Exception ignored) {
                return def;
            }
        }
    }


    public String getString(String key) {
        String s = "";
        try {
            s = sharedPreferences.getString(key, "");
        } catch (Exception ignored) {
        }
        return s;
    }

    public boolean getBoolean(String key, boolean def) {
        try {
            return sharedPreferences.getBoolean(key, def);
        } catch (Exception e) {
            return def;
        }
    }


    public int getFormatType() {
        return getInt(context.getString(R.string.key_format_type), 0);
    }

    public boolean installViaRootAccess() {
        return getBoolean(context.getString(R.string.pref_key_install_root), false);
    }
}
