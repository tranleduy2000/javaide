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

package com.android.ide.common.resources;

import com.android.ide.common.rendering.api.ResourceValue;
import com.android.ide.common.resources.configuration.FolderConfiguration;
import com.android.resources.ResourceType;


/**
 * Represents a resource item that has been declared inline in another resource file.
 *
 * This covers the typical ID declaration of "@+id/foo", but does not cover normal value
 * resources declared in strings.xml or other similar value files.
 *
 * This resource will return {@code true} for {@link #isDeclaredInline()} and {@code false} for
 * {@link #isEditableDirectly()}.
 */
public class InlineResourceItem extends ResourceItem {

    private ResourceValue mValue = null;

    /**
     * Constructs a new inline ResourceItem.
     * @param name the name of the resource as it appears in the XML and R.java files.
     */
    public InlineResourceItem(String name) {
        super(name);
    }

    @Override
    public boolean isDeclaredInline() {
        return true;
    }

    @Override
    public boolean isEditableDirectly() {
        return false;
    }

    @Override
    public ResourceValue getResourceValue(ResourceType type, FolderConfiguration referenceConfig,
            boolean isFramework) {
        assert type == ResourceType.ID;
        if (mValue == null) {
            mValue = new ResourceValue(type, getName(), isFramework);
        }

        return mValue;
    }

    @Override
    public String toString() {
        return "InlineResourceItem [mName=" + getName() + ", mFiles=" //$NON-NLS-1$ //$NON-NLS-2$
                + getSourceFileList() + "]"; //$NON-NLS-1$
    }
}
