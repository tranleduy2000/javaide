/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.sdklib.internal.build;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class able to generate a BuildConfig class in Android project.
 * The BuildConfig class contains constants related to the build target.
 */
public class BuildConfigGenerator {

    private final static String PH_PACKAGE = "#PACKAGE#";
    private final static String PH_DEBUG = "#DEBUG#";

    private final String mGenFolder;
    private final String mAppPackage;
    private final boolean mDebug;

    /**
     * Creates a generator
     * @param genFolder the gen folder of the project
     * @param appPackage the application package
     * @param debug whether it's a debug build
     */
    public BuildConfigGenerator(String genFolder, String appPackage, boolean debug) {
        mGenFolder = genFolder;
        mAppPackage = appPackage;
        mDebug = debug;
    }

    /**
     * Generates the BuildConfig class.
     */
    public void generate() throws IOException {
        String template = readEmbeddedTextFile("BuildConfig.template");

        Map<String, String> map = new HashMap<String, String>();
        map.put(PH_PACKAGE, mAppPackage);
        map.put(PH_DEBUG, Boolean.toString(mDebug));

        String content = replaceParameters(template, map);

        File genFolder = new File(mGenFolder);
        File pkgFolder = new File(genFolder, mAppPackage.replaceAll("\\.", File.separator));
        if (pkgFolder.isDirectory() == false) {
            pkgFolder.mkdirs();
        }

        File buildConfigJava = new File(pkgFolder, "BuildConfig.java");
        writeFile(buildConfigJava, content);
    }

    /**
     * Reads and returns the content of a text file embedded in the jar file.
     * @param filepath the file path to the text file
     * @return null if the file could not be read
     * @throws IOException
     */
    private String readEmbeddedTextFile(String filepath) throws IOException {
        InputStream is = BuildConfigGenerator.class.getResourceAsStream(filepath);
        if (is != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String line;
            StringBuilder total = new StringBuilder(reader.readLine());
            while ((line = reader.readLine()) != null) {
                total.append('\n');
                total.append(line);
            }

            return total.toString();
        }

        // this really shouldn't happen unless the sdklib packaging is broken.
        throw new IOException("BuildConfig template is missing!");
    }

    private void writeFile(File file, String content) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            InputStream source = new ByteArrayInputStream(content.getBytes("UTF-8"));

            byte[] buffer = new byte[1024];
            int count = 0;
            while ((count = source.read(buffer)) != -1) {
                fos.write(buffer, 0, count);
            }
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    /**
     * Replaces placeholders found in a string with values.
     *
     * @param str the string to search for placeholders.
     * @param parameters a map of <placeholder, Value> to search for in the string
     * @return A new String object with the placeholder replaced by the values.
     */
    private String replaceParameters(String str, Map<String, String> parameters) {

        for (Entry<String, String> entry : parameters.entrySet()) {
            String value = entry.getValue();
            if (value != null) {
                str = str.replaceAll(entry.getKey(), value);
            }
        }

        return str;
    }
}
