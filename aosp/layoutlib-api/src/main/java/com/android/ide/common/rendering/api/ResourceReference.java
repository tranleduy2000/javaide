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

package com.android.ide.common.rendering.api;

/**
 * A resource reference. This contains the String ID of the resource and whether this is a framework
 * reference.
 * This is an immutable class.
 *
 */
public class ResourceReference {
    private final String mName;
    private final boolean mIsFramework;

    /**
     * Builds a resource reference.
     * @param name the name of the resource
     * @param isFramework whether the reference is to a framework resource.
     */
    public ResourceReference(String name, boolean isFramework) {
        mName = name;
        mIsFramework = isFramework;
    }

    /**
     * Builds a non-framework resource reference.
     * @param name the name of the resource
     */
    public ResourceReference(String name) {
        this(name, false /*platformLayout*/);
    }

    /**
     * Returns the name of the resource, as defined in the XML.
     */
    public final String getName() {
        return mName;
    }

    /**
     * Returns whether the resource is a framework resource (<code>true</code>) or a project
     * resource (<code>false</false>).
     */
    public final boolean isFramework() {
        return mIsFramework;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (mIsFramework ? 1231 : 1237);
        result = prime * result + ((mName == null) ? 0 : mName.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ResourceReference other = (ResourceReference) obj;
        if (mIsFramework != other.mIsFramework)
            return false;
        if (mName == null) {
            if (other.mName != null)
                return false;
        } else if (!mName.equals(other.mName))
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ResourceReference [" + mName + " (framework:" + mIsFramework+ ")]";
    }
}
