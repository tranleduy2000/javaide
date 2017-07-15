/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.sdklib.internal.project;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;


/**
 * Settings for multiple APK generation.
 */
public class ApkSettings {
    private final static char CHAR_EQUAL = ':';
    private final static char CHAR_SEP = '|';
    private final static String STR_SEP = Pattern.quote(new String(new char[] { CHAR_SEP }));

    private boolean mSplitByDensity = false;
    private boolean mSplitByAbi = false;
    private final Map<String, String> mSplitByLocale;

    /**
     * Creates an ApkSettings and fills it from the project settings read from a
     * {@link ProjectProperties} file.
     */
    public ApkSettings(ProjectProperties properties) {
        mSplitByDensity = Boolean.parseBoolean(properties.getProperty(
                ProjectProperties.PROPERTY_SPLIT_BY_DENSITY));
        mSplitByAbi =  Boolean.parseBoolean(properties.getProperty(
                ProjectProperties.PROPERTY_SPLIT_BY_ABI));
        String locale = properties.getProperty(ProjectProperties.PROPERTY_SPLIT_BY_LOCALE);
        if (locale != null && locale.length() > 0) {
            mSplitByLocale = readLocaleFilters(locale);
        } else {
            mSplitByLocale = Collections.unmodifiableMap(new HashMap<String, String>());
        }
    }

    /**
     * Indicates whether APKs should be generate for each dpi level.
     */
    public boolean isSplitByDensity() {
        return mSplitByDensity;
    }

    public void setSplitByDensity(boolean split) {
        mSplitByDensity = split;
    }

    public boolean isSplitByAbi() {
        return mSplitByAbi;
    }

    public void setSplitByAbi(boolean split) {
        mSplitByAbi = split;
    }

    /**
     * Writes the receiver into a {@link ProjectPropertiesWorkingCopy}.
     * @param properties the {@link ProjectPropertiesWorkingCopy} in which to store the settings.
     */
    public void write(ProjectPropertiesWorkingCopy properties) {
        properties.setProperty(ProjectProperties.PROPERTY_SPLIT_BY_DENSITY,
                Boolean.toString(mSplitByDensity));
        properties.setProperty(ProjectProperties.PROPERTY_SPLIT_BY_ABI,
                Boolean.toString(mSplitByAbi));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ApkSettings) {
            ApkSettings objSettings = (ApkSettings) obj;
            return mSplitByDensity == objSettings.mSplitByDensity &&
                    mSplitByAbi == objSettings.mSplitByAbi;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Integer.valueOf(
                (mSplitByDensity ? 1 : 0) +
                (mSplitByAbi ? 2 : 0)).hashCode();
    }

    public static Map<String, String> readLocaleFilters(String locale) {
        HashMap<String, String> map = new HashMap<String, String>();
        String[] filters = locale.split(STR_SEP);
        for (String filter : filters) {
            int charPos = filter.indexOf(CHAR_EQUAL);
            if (charPos > 0) {
                map.put(filter.substring(0, charPos), filter.substring(charPos+1));
            }
        }

        return Collections.unmodifiableMap(map);
    }

    public static String writeLocaleFilters(Map<String, String> filterMap) {
        StringBuilder sb = new StringBuilder();

        boolean first = true;
        for (Entry<String, String> entry : filterMap.entrySet()) {
            if (first == false) {
                sb.append(CHAR_SEP);
            }
            first = false;

            sb.append(entry.getKey());
            sb.append(CHAR_EQUAL);
            sb.append(entry.getValue());
        }

        return sb.toString();
    }

    public Map<String, String> getLocaleFilters() {
        return mSplitByLocale;
    }
}
