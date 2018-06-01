/*
 * Copyright (C) 2016 Jecelyin Peng <jecelyin@gmail.com>
 *
 * This file is part of 920 Text Editor.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jecelyin.common.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.os.Environment;
import android.util.TypedValue;

import com.jecelyin.common.BuildConfig;

/**
 * @author Jecelyin Peng <jecelyin@gmail.com>
 */
public class SysUtils {
    public static boolean isDebug() {
        return BuildConfig.DEBUG;
    }

    public static int dpAsPixels(Context context, int dp) {
        Resources resources = context.getResources();
        return (int) TypedValue.applyDimension(1, dp, resources.getDisplayMetrics());
    }

    public static byte[] getSignature(Context context) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            L.e(e);
        }
        if (packageInfo == null || packageInfo.signatures == null)
            return null;
        Signature signature = packageInfo.signatures[0];
        return signature.toByteArray();
    }

}
