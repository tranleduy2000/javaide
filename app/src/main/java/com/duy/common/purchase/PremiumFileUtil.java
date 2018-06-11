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

package com.duy.common.purchase;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Created by Duy on 9/15/2017.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
class PremiumFileUtil {

    private static final String TAG = "FileUtil";
    private static final String LICENSE_FILE_NAME = "license";

    @Nullable
    private static String readFile(File file) {
        try {
            FileInputStream inputStream = new FileInputStream(file);
            String result = IOUtils.toString(inputStream);
            inputStream.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static void saveLicence(@NonNull Context context) {
        String content = StringXor.encode(Installation.id(context));
        try {
            File file = new File(context.getFilesDir(), LICENSE_FILE_NAME);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(content.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write licenced cache
     */
    static boolean licenseCached(@NonNull Context context) {
        return licenseCachedCompat(context);
    }

    /**
     * Compatible with old version
     */
    private static boolean licenseCachedCompat(@NonNull Context context) {
        File file = new File(context.getFilesDir(), LICENSE_FILE_NAME);
        if (file.exists()) {
            String content = readFile(file);
            if (content != null && !content.isEmpty()) {
                content = StringXor.decode(content);
                if (content.equals(Installation.id(context))) {
                    return true;
                }
            }
        }

        //compatible with older version
        {
            file = new File(context.getCacheDir(), LICENSE_FILE_NAME);
            if (file.exists()) {
                String content = readFile(file);
                if (content != null && !content.isEmpty()) {
                    content = StringXor.decode(content);
                    if (content.equals(Installation.id(context))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    static void clearLicence(Context context) {
        File file = new File(context.getCacheDir(), LICENSE_FILE_NAME);
        if (file.exists()) {
            file.delete();
        }
        file = new File(context.getFilesDir(), LICENSE_FILE_NAME);
        if (file.exists()) {
            file.delete();
        }
    }
}
