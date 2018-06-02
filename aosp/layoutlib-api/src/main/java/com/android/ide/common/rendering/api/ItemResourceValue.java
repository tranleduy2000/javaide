/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.android.ide.common.rendering.api;

/**
 * Represents each item in the android style resource.
 */
public class ItemResourceValue extends ResourceValue {

    private final boolean mIsFrameworkAttr;

    /**
     * @see #ItemResourceValue(String, boolean, String, boolean)
     */
    public ItemResourceValue(String name, boolean isFrameworkAttr, boolean isFrameworkStyle) {
        this(name, isFrameworkAttr, null, isFrameworkStyle);
    }

    /**
     * If the value is a reference to a framework resource or not is NOT represented with a boolean!
     * but can be deduced with:
     * <pre> {@code
     *        boolean isFrameworkValue = item.isFramework() ||
     *            item.getValue().startsWith(SdkConstants.ANDROID_PREFIX) ||
     *            item.getValue().startsWith(SdkConstants.ANDROID_THEME_PREFIX);
     * } </pre>
     * For {@code <item name="foo">bar</item>}, item in a style resource, the values of the
     * parameters will be as follows:
     *
     * @param attributeName   foo
     * @param isFrameworkAttr is foo in framework namespace.
     * @param value           bar (in case of a reference, the value may include the namespace.
     *                        if the namespace is absent, default namespace is assumed based on
     *                        isFrameworkStyle (android namespace when isFrameworkStyle=true and app
     *                        namespace when isFrameworkStyle=false))
     * @param isFrameworkStyle if the style is a framework file or project file.
     */
    public ItemResourceValue(String attributeName, boolean isFrameworkAttr, String value,
            boolean isFrameworkStyle) {
        super(null, attributeName, value, isFrameworkStyle);
        mIsFrameworkAttr = isFrameworkAttr;
    }

    public boolean isFrameworkAttr() {
        return mIsFrameworkAttr;
    }

    Attribute getAttribute() {
        return new Attribute(getName(), mIsFrameworkAttr);
    }

    static ItemResourceValue fromResourceValue(ResourceValue res, boolean isFrameworkAttr) {
        assert res.getResourceType() == null : res.getResourceType() + " is not null";
        return new ItemResourceValue(res.getName(), isFrameworkAttr, res.getValue(),
                res.isFramework());
    }

    static final class Attribute {
        String mName;
        boolean mIsFrameworkAttr;

        Attribute(String name, boolean isFrameworkAttr) {
            mName = name;
            mIsFrameworkAttr = isFrameworkAttr;
        }

        @Override
        public int hashCode() {
            int booleanHash = mIsFrameworkAttr ? 1231 : 1237;  // see java.lang.Boolean#hashCode()
            return 31 * booleanHash + mName.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Attribute) {
                Attribute attr = (Attribute) obj;
                return mIsFrameworkAttr == attr.mIsFrameworkAttr && mName.equals(attr.mName);
            }
            return false;
        }

        @Override
        public String toString() {
            return mName + " (framework:" + mIsFrameworkAttr + ")";
        }
    }
}
