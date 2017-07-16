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

package com.duy.editor.view.exec_screen.console;

import com.duy.editor.DLog;

/**
 * Created by Duy on 26-Mar-17.
 */

@SuppressWarnings("DefaultFileTemplate")
public class ConsoleDebug {
    public final static String TAG = ConsoleDebug.class.getSimpleName();
    private static boolean DEBUG = false;

    public static void log(char c) {
        if (DEBUG) DLog.d(TAG, "emit char: " + Character.toString(c));
    }

    public static void log(String s) {
        if (DEBUG) DLog.d(TAG, "emit string: " + s);
    }

    public static String bytesToString(byte[] data, int base, int length) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < length; i++) {
            byte b = data[base + i];
            if (b < 32 || b > 126) {
                buf.append(String.format("\\x%02x", b));
            } else {
                buf.append((char) b);
            }
        }
        return buf.toString();
    }
}
