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

import com.android.resources.ResourceType;

/**
 * A {@link com.android.ide.common.rendering.api.ResourceValue} intended for text nodes
 * where we need access to the raw XML text
 */
public class TextResourceValue extends ResourceValue {
    private String mRawXmlValue;

    public TextResourceValue(ResourceType type, String name, boolean isFramework) {
        super(type, name, isFramework);
    }

    public TextResourceValue(ResourceType type, String name, String textValue, String rawXmlValue,
            boolean isFramework) {
        super(type, name, textValue, isFramework);
        mRawXmlValue = rawXmlValue;
    }

    @Override
    public String getRawXmlValue() {
        if (mRawXmlValue != null) {
            return mRawXmlValue;
        }
        return super.getValue();
    }

    public void setRawXmlValue(String value) {
        mRawXmlValue = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((mRawXmlValue == null) ? 0 : mRawXmlValue.hashCode());
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
        TextResourceValue other = (TextResourceValue) obj;
        if (mRawXmlValue == null) {
            //noinspection VariableNotUsedInsideIf
            if (other.mRawXmlValue != null)
                return false;
        } else if (!mRawXmlValue.equals(other.mRawXmlValue))
            return false;
        return true;
    }

}
