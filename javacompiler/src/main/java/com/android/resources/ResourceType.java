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

package com.android.resources;


/**
 * Enum representing a type of compiled resource.
 */
public enum ResourceType {
    ANIM("anim", "Animation"), //$NON-NLS-1$
    ANIMATOR("animator", "Animator"), //$NON-NLS-1$
    ARRAY("array", "Array", "string-array", "integer-array"), //$NON-NLS-1$ //$NON-NLS-3$ //$NON-NLS-4$
    ATTR("attr", "Attr"), //$NON-NLS-1$
    BOOL("bool", "Boolean"), //$NON-NLS-1$
    COLOR("color", "Color"), //$NON-NLS-1$
    DECLARE_STYLEABLE("declare-styleable", "Declare Stylable"), //$NON-NLS-1$
    DIMEN("dimen", "Dimension"), //$NON-NLS-1$
    DRAWABLE("drawable", "Drawable"), //$NON-NLS-1$
    FRACTION("fraction", "Fraction"), //$NON-NLS-1$
    ID("id", "ID"), //$NON-NLS-1$
    INTEGER("integer", "Integer"), //$NON-NLS-1$
    INTERPOLATOR("interpolator", "Interpolator"), //$NON-NLS-1$
    LAYOUT("layout", "Layout"), //$NON-NLS-1$
    MENU("menu", "Menu"), //$NON-NLS-1$
    MIPMAP("mipmap", "Mip Map"), //$NON-NLS-1$
    PLURALS("plurals", "Plurals"), //$NON-NLS-1$
    RAW("raw", "Raw"), //$NON-NLS-1$
    STRING("string", "String"), //$NON-NLS-1$
    STYLE("style", "Style"), //$NON-NLS-1$
    STYLEABLE("styleable", "Styleable"), //$NON-NLS-1$
    XML("xml", "XML"), //$NON-NLS-1$
    // this is not actually used. Only there because they get parsed and since we want to
    // detect new resource type, we need to have this one exist.
    PUBLIC("public", "###"); //$NON-NLS-1$ //$NON-NLS-2$

    private final String mName;
    private final String mDisplayName;
    private final String[] mAlternateXmlNames;

    ResourceType(String name, String displayName, String... alternateXmlNames) {
        mName = name;
        mDisplayName = displayName;
        mAlternateXmlNames = alternateXmlNames;
    }

    /**
     * Returns the resource type name, as used by XML files.
     */
    public String getName() {
        return mName;
    }

    /**
     * Returns a translated display name for the resource type.
     */
    public String getDisplayName() {
        return mDisplayName;
    }

    /**
     * Returns the enum by its name as it appears in the XML or the R class.
     * @param name name of the resource
     * @return the matching {@link ResourceType} or <code>null</code> if no match was found.
     */
    public static ResourceType getEnum(String name) {
        for (ResourceType rType : values()) {
            if (rType.mName.equals(name)) {
                return rType;
            } else if (rType.mAlternateXmlNames != null) {
                // if there are alternate Xml Names, we test those too
                for (String alternate : rType.mAlternateXmlNames) {
                    if (alternate.equals(name)) {
                        return rType;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns an array with all the names defined by this enum.
     */
    public static String[] getNames() {
        ResourceType[] values = values();
        String[] names = new String[values.length];
        for (int i = values.length - 1; i >= 0; --i) {
            names[i] = values[i].getName();
        }
        return names;
    }

    @Override
    public String toString() {
        return getName();
    }
}
