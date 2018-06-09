/*
 * Copyright (c) 2018 by Tran Le Duy
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
