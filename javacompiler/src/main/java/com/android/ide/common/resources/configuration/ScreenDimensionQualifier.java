/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.android.ide.common.resources.configuration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resource Qualifier for Screen Dimension.
 */
public final class ScreenDimensionQualifier extends ResourceQualifier {
    /** Default screen size value. This means the property is not set */
    static final int DEFAULT_SIZE = -1;

    private static final Pattern sDimensionPattern = Pattern.compile(
            "^(\\d+)x(\\d+)$"); //$NON-NLS-1$

    public static final String NAME = "Screen Dimension";

    /** Screen size 1 value. This is not size X or Y because the folder name always
     * contains the biggest size first. So if the qualifier is 400x200, size 1 will always be
     * 400 but that will be X in landscape and Y in portrait.
     * Default value is <code>DEFAULT_SIZE</code> */
    private int mValue1 = DEFAULT_SIZE;

    /** Screen size 2 value. This is not size X or Y because the folder name always
     * contains the biggest size first. So if the qualifier is 400x200, size 2 will always be
     * 200 but that will be Y in landscape and X in portrait.
     * Default value is <code>DEFAULT_SIZE</code> */
    private int mValue2 = DEFAULT_SIZE;

    public ScreenDimensionQualifier() {
        // pass
    }

    public ScreenDimensionQualifier(int value1, int value2) {
        mValue1 = value1;
        mValue2 = value2;
    }

    public int getValue1() {
        return mValue1;
    }

    public int getValue2() {
        return mValue2;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getShortName() {
        return "Dimension";
    }

    @Override
    public int since() {
        return 1;
    }

    @Override
    public boolean deprecated() {
        return true;
    }

    @Override
    public boolean isValid() {
        return mValue1 != DEFAULT_SIZE && mValue2 != DEFAULT_SIZE;
    }

    @Override
    public boolean hasFakeValue() {
        return false;
    }

    @Override
    public boolean checkAndSet(String value, FolderConfiguration config) {
        Matcher m = sDimensionPattern.matcher(value);
        if (m.matches()) {
            String d1 = m.group(1);
            String d2 = m.group(2);

            ScreenDimensionQualifier qualifier = getQualifier(d1, d2);
            if (qualifier != null) {
                config.setScreenDimensionQualifier(qualifier);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object qualifier) {
        if (qualifier instanceof ScreenDimensionQualifier) {
            ScreenDimensionQualifier q = (ScreenDimensionQualifier)qualifier;
            return (mValue1 == q.mValue1 && mValue2 == q.mValue2);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public static ScreenDimensionQualifier getQualifier(String size1, String size2) {
        try {
            int s1 = Integer.parseInt(size1);
            int s2 = Integer.parseInt(size2);

            ScreenDimensionQualifier qualifier = new ScreenDimensionQualifier();

            if (s1 > s2) {
                qualifier.mValue1 = s1;
                qualifier.mValue2 = s2;
            } else {
                qualifier.mValue1 = s2;
                qualifier.mValue2 = s1;
            }

            return qualifier;
        } catch (NumberFormatException e) {
            // looks like the string we extracted wasn't a valid number.
        }

        return null;
    }

    /**
     * Returns the string used to represent this qualifier in the folder name.
     */
    @Override
    public String getFolderSegment() {
        return String.format("%1$dx%2$d", mValue1, mValue2); //$NON-NLS-1$
    }

    @Override
    public String getShortDisplayValue() {
        if (isValid()) {
            return String.format("%1$dx%2$d", mValue1, mValue2);
        }

        return ""; //$NON-NLS-1$
    }

    @Override
    public String getLongDisplayValue() {
        if (isValid()) {
            return String.format("Screen resolution %1$dx%2$d", mValue1, mValue2);
        }

        return ""; //$NON-NLS-1$
    }
}
