/*
 * Copyright (C) 2008 The Android Open Source Project
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

import com.android.layoutlib.api.IResourceValue;
import com.android.resources.ResourceType;

/**
 * Represents an android resource with a name and a string value.
 */
@SuppressWarnings("deprecation")
public class ResourceValue extends ResourceReference implements IResourceValue {
    private final ResourceType mType;
    protected String mValue = null;

    public ResourceValue(ResourceType type, String name, boolean isFramework) {
        super(name, isFramework);
        mType = type;
    }

    public ResourceValue(ResourceType type, String name, String value, boolean isFramework) {
        super(name, isFramework);
        mType = type;
        mValue = value;
    }

    public ResourceType getResourceType() {
        return mType;
    }

    /**
     * Returns the type of the resource. For instance "drawable", "color", etc...
     * @deprecated use {@link #getResourceType()} instead.
     */
    @Override
    @Deprecated
    public String getType() {
        return mType.getName();
    }

    /**
     * Returns the value of the resource, as defined in the XML. This can be <code>null</code>
     */
    @Override
    public String getValue() {
        return mValue;
    }

    /**
     * Similar to {@link #getValue()}, but returns the raw XML value. This is <b>usually</b>
     * the same as getValue, but with a few exceptions. For example, for markup strings,
     * you can have * {@code <string name="markup">This is <b>bold</b></string>}.
     * Here, {@link #getValue()} will return "{@code This is bold}" -- e.g. just
     * the plain text flattened. However, this method will return "{@code This is <b>bold</b>}",
     * which preserves the XML markup elements.
     */
    public String getRawXmlValue() {
        return getValue();
    }

    /**
     * Sets the value of the resource.
     * @param value the new value
     */
    public void setValue(String value) {
        mValue = value;
    }

    /**
     * Sets the value from another resource.
     * @param value the resource value
     */
    public void replaceWith(ResourceValue value) {
        mValue = value.mValue;
    }

    @Override
    public String toString() {
        return "ResourceValue [" + mType + "/" + getName() + " = " + mValue  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + " (framework:" + isFramework() + ")]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((mType == null) ? 0 : mType.hashCode());
        result = prime * result + ((mValue == null) ? 0 : mValue.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        ResourceValue other = (ResourceValue) obj;
        if (mType == null) {
            //noinspection VariableNotUsedInsideIf
            if (other.mType != null)
                return false;
        } else if (!mType.equals(other.mType))
            return false;
        if (mValue == null) {
            //noinspection VariableNotUsedInsideIf
            if (other.mValue != null)
                return false;
        } else if (!mValue.equals(other.mValue))
            return false;
        return true;
    }
}
