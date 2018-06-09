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

package com.duy.ide.java.setting;

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

    @SuppressLint("CommitPrefEdits")
    public AppSetting(@NonNull SharedPreferences mPreferences, @NonNull Context context) {
        this.context = context;
        this.sharedPreferences = mPreferences;
        this.editor = sharedPreferences.edit();
    }

    /**
     * reset default setting
     *
     * @param context
     */
    public static void setFirstOpen(Context context) {
        String key = "first_open";
    }

    @NonNull
    public Context getContext() {
        return context;
    }

    @NonNull
    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public void setSharedPreferences(@NonNull SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public void put(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public void put(String key, int value) {
        editor.putInt(key, value);
        editor.commit();
    }

    public void put(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }

    public void put(String key, long value) {
        editor.putLong(key, value);
        editor.commit();
    }

    public void put(String key, float value) {
        editor.putFloat(key, value);
        editor.commit();
    }

    public int getInt(String key) {
        return getInt(key, -1);
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

    /**
     * get long value from key,
     *
     * @param key - key
     * @return -1 if not found
     */
    public long getLong(String key) {
        try {
            return sharedPreferences.getLong(key, -1);
        } catch (Exception e) {
            try {
                return Long.parseLong(getString(key));
            } catch (Exception ignored) {
            }
        }
        return -1;
    }

    public String getString(String key) {
        String s = "";
        try {
            s = sharedPreferences.getString(key, "");
        } catch (Exception ignored) {
        }
        return s;
    }

    public boolean getBoolean(String key) {
        try {
            return sharedPreferences.getBoolean(key, false);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean getBoolean(String key, boolean def) {
        try {
            return sharedPreferences.getBoolean(key, def);
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * @return size of editor text in sp unit
     */
    public float getEditorTextSize() {
        try {
            return Float.parseFloat(getString(context.getString(R.string.key_pref_font_size)));
        } catch (Exception e) {
            return 12f;
        }
    }

    public void setTheme(String name) {
        put(context.getString(R.string.key_code_theme), name);
    }



    public int getFormatType() {
        return getInt(context.getString(R.string.key_format_type), 0);
    }

    public String getTab() {
        return "  ";
    }

    public boolean installViaRootAccess() {
        return getBoolean(context.getString(R.string.pref_key_install_root), false);
    }
}
