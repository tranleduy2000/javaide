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

package com.duy.frontend.keyboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.KeyEvent;

import com.duy.frontend.R;

import java.util.Arrays;

public class KeySettings {
    /**
     * An integer not in the range of real key codes.
     */
    public static final int KEYCODE_NONE = -1;
    public static final int[] CONTROL_KEY_SCHEMES = {
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_CAMERA,
            KEYCODE_NONE
    };
    private SharedPreferences mPrefs;
    private int mControlKeyId = 0; // Default to Volume Down
    private Context context;

    public KeySettings(SharedPreferences prefs, Context context) {
        this.context = context;
        readPrefs(prefs);
    }

    public void readPrefs(SharedPreferences prefs) {
        mPrefs = prefs;
        String s = readStringPref(context.getString(R.string.key_pref_control), context.getString(R.string.volume_down));
        String[] array = context.getResources().getStringArray(R.array.control_key);
        mControlKeyId = Arrays.asList(array).indexOf(s);
    }

    private String readStringPref(String key, String defaultValue) {
        return mPrefs.getString(key, defaultValue);
    }

    public int getControlKeyId() {
        return mControlKeyId;
    }

    public int getControlKeyCode() {
        return CONTROL_KEY_SCHEMES[mControlKeyId];
    }

}
