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

package com.duy.ide.java.file;

import android.os.Environment;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * File Manager
 * Created by Duy on 10-Feb-17.
 */
public class FileManager {
    public static final String EXTERNAL_DIR_SRC;
    public static final String EXTERNAL_DIR;

    static {
        EXTERNAL_DIR_SRC = Environment.getExternalStorageDirectory() + "/JavaNIDE/src/";
        EXTERNAL_DIR = new File(Environment.getExternalStorageDirectory(), "JavaNIDE").getAbsolutePath();
    }

    public static ArrayList<String> listClassName(File src) {
        if (!src.exists()) return new ArrayList<>();

        String[] exts = new String[]{"java"};
        Collection<File> files = FileUtils.listFiles(src, exts, true);

        ArrayList<String> classes = new ArrayList<>();
        String srcPath = src.getPath();
        for (File file : files) {
            String javaPath = file.getPath();
            javaPath = javaPath.substring(srcPath.length() + 1, javaPath.length() - 5); //.java
            javaPath = javaPath.replace(File.separator, ".");
            classes.add(javaPath);
        }
        return classes;
    }


}
