/*
 * Copyright (C) 2013 The Android Open Source Project
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

import static com.android.SdkConstants.ANDROID_NS_NAME;
import static com.android.SdkConstants.ATTR_REF_PREFIX;
import static com.android.SdkConstants.PREFIX_RESOURCE_REF;
import static com.android.SdkConstants.PREFIX_THEME_REF;
import static com.android.SdkConstants.RESOURCE_CLZ_ATTR;
import static com.android.ide.common.rendering.api.RenderResources.REFERENCE_EMPTY;
import static com.android.ide.common.rendering.api.RenderResources.REFERENCE_NULL;
import static com.android.ide.common.rendering.api.RenderResources.REFERENCE_UNDEFINED;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.ide.common.rendering.api.ResourceValue;
import com.android.resources.ResourceType;

/**
 * A {@linkplain ResourceUrl} represents a parsed resource url such as {@code @string/foo} or
 * {@code ?android:attr/bar}
 */
public class ResourceUrl {
    /** Type of resource */
    @NonNull public final ResourceType type;

    /** Name of resource */
    @NonNull public final String name;

    /** If true, the resource is in the android: framework */
    public final boolean framework;

    /** Whether an id resource is of the form {@code @+id} rather than just {@code @id} */
    public final boolean create;

    /** Whether this is a theme resource reference */
    public final boolean theme;

    private ResourceUrl(@NonNull ResourceType type, @NonNull String name,
            boolean framework, boolean create, boolean theme) {
        this.type = type;
        this.name = name;
        this.framework = framework;
        this.create = create;
        this.theme = theme;
    }

    /**
     * Creates a new resource URL. Normally constructed via {@link #parse(String)}.
     *
     * @param type the resource type
     * @param name the name
     * @param framework whether it's a framework resource
     * @param create if it's an id resource, whether it's of the form {@code @+id}
     */
    public static ResourceUrl create(@NonNull ResourceType type, @NonNull String name,
            boolean framework, boolean create) {
        return new ResourceUrl(type, name, framework, create, false);
    }

    public static ResourceUrl create(@NonNull ResourceValue value) {
        return create(value.getResourceType(), value.getName(), value.isFramework(), false);
    }

    /**
     * Return the resource type of the given url, and the resource name
     *
     * @param url the resource url to be parsed
     * @return a pair of the resource type and the resource name
     */
    @Nullable
    public static ResourceUrl parse(@NonNull String url) {
        return parse(url, false);
    }

    /**
     * Return the resource type of the given url, and the resource name.
     *
     * @param url the resource url to be parsed
     * @param forceFramework force the returned value to be a framework resource.
     * @return a pair of the resource type and the resource name
     */
    @Nullable
    public static ResourceUrl parse(@NonNull String url, boolean forceFramework) {
        boolean isTheme = false;
        // Handle theme references
        if (url.startsWith(PREFIX_THEME_REF)) {
            isTheme = true;
            String remainder = url.substring(PREFIX_THEME_REF.length());
            if (url.startsWith(ATTR_REF_PREFIX)) {
                url = PREFIX_RESOURCE_REF + url.substring(PREFIX_THEME_REF.length());
            } else {
                int colon = url.indexOf(':');
                if (colon != -1) {
                    // Convert from ?android:progressBarStyleBig to ?android:attr/progressBarStyleBig
                    if (remainder.indexOf('/', colon) == -1) {
                        remainder = remainder.substring(0, colon) + RESOURCE_CLZ_ATTR + '/'
                                + remainder.substring(colon);
                    }
                    url = PREFIX_RESOURCE_REF + remainder;
                } else {
                    int slash = url.indexOf('/');
                    if (slash == -1) {
                        url = PREFIX_RESOURCE_REF + RESOURCE_CLZ_ATTR + '/' + remainder;
                    }
                }
            }
        }

        if (!url.startsWith(PREFIX_RESOURCE_REF) || isNullOrEmpty(url)) {
            return null;
        }

        int typeEnd = url.indexOf('/', 1);
        if (typeEnd == -1) {
            return null;
        }
        int nameBegin = typeEnd + 1;

        // Skip @ and @+
        boolean create = url.startsWith("@+");
        int typeBegin = create ? 2 : 1;

        int colon = url.lastIndexOf(':', typeEnd);
        boolean framework = forceFramework;
        if (colon != -1) {
            if (url.startsWith(ANDROID_NS_NAME, typeBegin)) {
                framework = true;
            }
          typeBegin = colon + 1;
        }
        String typeName = url.substring(typeBegin, typeEnd);
        ResourceType type = ResourceType.getEnum(typeName);
        if (type == null) {
            return null;
        }
        String name = url.substring(nameBegin);
        return new ResourceUrl(type, name, framework, create, isTheme);
    }

    /** Returns if the resource url is @null, @empty or @undefined. */
    public static boolean isNullOrEmpty(@NonNull String url) {
        return url.equals(REFERENCE_NULL) || url.equals(REFERENCE_EMPTY) ||
                url.equals(REFERENCE_UNDEFINED);
    }

    /**
     * Checks whether this resource has a valid name. Used when parsing data that isn't
     * necessarily known to be a valid resource; for example, "?attr/hello world"
     */
    public boolean hasValidName() {
        // Make sure it looks like a resource name; if not, it could just be a string
        // which starts with a ?, etc.
        if (name.isEmpty()) {
            return false;
        }

        if (!Character.isJavaIdentifierStart(name.charAt(0))) {
            return false;
        }
        for (int i = 1, n = name.length(); i < n; i++) {
            char c = name.charAt(i);
            if (!Character.isJavaIdentifierPart(c) && c != '.') {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(theme ? PREFIX_THEME_REF : PREFIX_RESOURCE_REF);
        if (create) {
            sb.append('+');
        }
        if (framework) {
            sb.append(ANDROID_NS_NAME);
            sb.append(':');
        }
        sb.append(type.getName());
        sb.append('/');
        sb.append(name);
        return sb.toString();
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ResourceUrl that = (ResourceUrl) o;

        if (create != that.create) {
            return false;
        }
        if (framework != that.framework) {
            return false;
        }
        if (!name.equals(that.name)) {
            return false;
        }
        if (type != that.type) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + (framework ? 1 : 0);
        result = 31 * result + (create ? 1 : 0);
        return result;
    }
}
