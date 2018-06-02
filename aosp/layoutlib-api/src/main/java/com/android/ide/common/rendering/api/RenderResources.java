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

import com.android.resources.ResourceType;

import java.util.List;

/**
 * A class containing all the resources needed to do a rendering.
 * <p/>
 * This contains both the project specific resources and the framework resources, and provide
 * convenience methods to resolve resource and theme references.
 */
public class RenderResources {

    public static final String REFERENCE_NULL = "@null";
    public static final String REFERENCE_EMPTY = "@empty";
    public static final String REFERENCE_UNDEFINED = "@undefined";

    public static class FrameworkResourceIdProvider {
        public Integer getId(ResourceType resType, String resName) {
            return null;
        }
    }

    public void setFrameworkResourceIdProvider(FrameworkResourceIdProvider provider) {
    }

    public void setLogger(LayoutLog logger) {
    }

    /**
     * Returns the {@link StyleResourceValue} representing the current theme.
     * @return the theme or null if there is no current theme.
     * @deprecated Use {@link #getDefaultTheme()} or {@link #getAllThemes()}
     */
    @Deprecated
    public StyleResourceValue getCurrentTheme() {
        // Default theme is same as the current theme was on older versions of the API.
        // With the introduction of applyStyle() "current" theme makes little sense.
        // Hence, simply return defaultTheme.
        return getDefaultTheme();
    }

    /**
     * Returns the {@link StyleResourceValue} representing the default theme.
     */
    public StyleResourceValue getDefaultTheme() {
        return null;
    }

    /**
     * Use this theme to resolve resources.
     * <p/>
     * Remember to call {@link #clearStyles()} to clear the applied styles, so the default theme
     * may be restored.
     *
     * @param theme The style to use for resource resolution in addition to the the default theme
     *      and the styles applied earlier. If null, the operation is a no-op.
     * @param useAsPrimary If true, the {@code theme} is used first to resolve attributes. If
     *      false, the theme is used if the resource cannot be resolved using the default theme and
     *      all the themes that have been applied prior to this call.
     */
    public void applyStyle(StyleResourceValue theme, boolean useAsPrimary) {
    }

    /**
     * Clear all the themes applied with {@link #applyStyle(StyleResourceValue, boolean)}
     */
    public void clearStyles() {
    }

    /**
     * Returns a list of {@link StyleResourceValue} containing a list of themes to be used for
     * resolving resources. The order of the themes in the list specifies the order in which they
     * should be used to resolve resources.
     */
    public List<StyleResourceValue> getAllThemes() {
       return null;
    }

    /**
     * Returns a theme by its name.
     *
     * @param name the name of the theme
     * @param frameworkTheme whether the theme is a framework theme.
     * @return the theme or null if there's no match
     */
    public StyleResourceValue getTheme(String name, boolean frameworkTheme) {
        return null;
    }

    /**
     * Returns whether a theme is a parent of a given theme.
     * @param parentTheme the parent theme
     * @param childTheme the child theme.
     * @return true if the parent theme is indeed a parent theme of the child theme.
     */
    public boolean themeIsParentOf(StyleResourceValue parentTheme, StyleResourceValue childTheme) {
        return false;
    }

    /**
     * Returns a framework resource by type and name. The returned resource is resolved.
     * @param resourceType the type of the resource
     * @param resourceName the name of the resource
     */
    public ResourceValue getFrameworkResource(ResourceType resourceType, String resourceName) {
        return null;
    }

    /**
     * Returns a project resource by type and name. The returned resource is resolved.
     * @param resourceType the type of the resource
     * @param resourceName the name of the resource
     */
    public ResourceValue getProjectResource(ResourceType resourceType, String resourceName) {
        return null;
    }

    /**
     * Returns the {@link ResourceValue} matching a given name in the all themes returned by
     * {@link #getAllThemes()}. If the item is not directly available in the a theme, its parent
     * theme is used before checking the next theme from the list.
     *
     * @param itemName the name of the item to search for.
     * @return the {@link ResourceValue} object or <code>null</code>
     *
     * @deprecated Use {@link #findItemInTheme(String, boolean)}
     */
    @Deprecated
    public ResourceValue findItemInTheme(String itemName) {
        List<StyleResourceValue> allThemes = getAllThemes();
        if (allThemes == null) {
            return null;
        }
        for (StyleResourceValue theme : allThemes) {
            //noinspection deprecation
            ResourceValue value = findItemInStyle(theme, itemName);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /**
     * Returns the {@link ResourceValue} matching a given name in the all themes returned by
     * {@link #getAllThemes()}. If the item is not directly available in the a theme, its parent
     * theme is used before checking the next theme from the list.
     *
     * @param attrName the name of the attribute to search for.
     * @param isFrameworkAttr whether the attribute is a framework attribute
     * @return the {@link ResourceValue} object or <code>null</code>
     */
    public ResourceValue findItemInTheme(String attrName, boolean isFrameworkAttr) {
        List<StyleResourceValue> allThemes = getAllThemes();
        if (allThemes == null) {
            return null;
        }
        for (StyleResourceValue theme : allThemes) {
            ResourceValue value = findItemInStyle(theme, attrName, isFrameworkAttr);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /**
     * Returns the {@link ResourceValue} matching a given name in a given style. If the
     * item is not directly available in the style, the method looks in its parent style.
     *
     * This version of doesn't support providing the namespace of the attribute so it'll search
     * in both the project's namespace and then in the android namespace.
     *
     * @param style the style to search in
     * @param attrName the name of the attribute to search for.
     * @return the {@link ResourceValue} object or <code>null</code>
     *
     * @deprecated Use {@link #findItemInStyle(StyleResourceValue, String, boolean)} since this
     * method doesn't know the item namespace.
     */
    @Deprecated
    public ResourceValue findItemInStyle(StyleResourceValue style, String attrName) {
        return null;
    }

    /**
     * Returns the {@link ResourceValue} matching a given attribute in a given style. If the
     * item is not directly available in the style, the method looks in its parent style.
     *
     * @param style the style to search in
     * @param attrName the name of the attribute to search for.
     * @param isFrameworkAttr whether the attribute is a framework attribute
     * @return the {@link ResourceValue} object or <code>null</code>
     */
    public ResourceValue findItemInStyle(StyleResourceValue style, String attrName,
            boolean isFrameworkAttr) {
        return null;
    }

    /**
     * Searches for, and returns a {@link ResourceValue} by its reference.
     * <p/>
     * The reference format can be:
     * <pre>@resType/resName</pre>
     * <pre>@android:resType/resName</pre>
     * <pre>@resType/android:resName</pre>
     * <pre>?resType/resName</pre>
     * <pre>?android:resType/resName</pre>
     * <pre>?resType/android:resName</pre>
     * Any other string format will return <code>null</code>.
     * <p/>
     * The actual format of a reference is <pre>@[namespace:]resType/resName</pre> but this method
     * only support the android namespace.
     *
     * @param reference the resource reference to search for.
     * @param forceFrameworkOnly if true all references are considered to be toward framework
     *      resource even if the reference does not include the android: prefix.
     * @return a {@link ResourceValue} or <code>null</code>.
     */
    public ResourceValue findResValue(String reference, boolean forceFrameworkOnly) {
        return null;
    }

    /**
     * Resolves the value of a resource, if the value references a theme or resource value.
     * <p/>
     * This method ensures that it returns a {@link ResourceValue} object that does not
     * reference another resource.
     * If the resource cannot be resolved, it returns <code>null</code>.
     * <p/>
     * If a value that does not need to be resolved is given, the method will return a new
     * instance of {@link ResourceValue} that contains the input value.
     *
     * @param type the type of the resource
     * @param name the name of the attribute containing this value.
     * @param value the resource value, or reference to resolve
     * @param isFrameworkValue whether the value is a framework value.
     *
     * @return the resolved resource value or <code>null</code> if it failed to resolve it.
     */
    public ResourceValue resolveValue(ResourceType type, String name, String value,
            boolean isFrameworkValue) {
        return null;
    }

    /**
     * Returns the {@link ResourceValue} referenced by the value of <var>value</var>.
     * <p/>
     * This method ensures that it returns a {@link ResourceValue} object that does not
     * reference another resource.
     * If the resource cannot be resolved, it returns <code>null</code>.
     * <p/>
     * If a value that does not need to be resolved is given, the method will return the input
     * value.
     *
     * @param value the value containing the reference to resolve.
     * @return a {@link ResourceValue} object or <code>null</code>
     */
    public ResourceValue resolveResValue(ResourceValue value) {
        return null;
    }

    /**
     * Returns the parent style of the given style, if any
     * @param style the style to look up
     * @return the parent style, or null
     */
    public StyleResourceValue getParent(StyleResourceValue style) {
        return null;
    }

    /**
     * Returns the style matching the given name. The name should not contain any namespace prefix.
     * @param styleName Name of the style. For example, "Widget.ListView.DropDown".
     * @return the {link StyleResourceValue} for the style, or null if not found.
     */
    public StyleResourceValue getStyle(String styleName, boolean isFramework) {
         return null;
     }

    /**
     * Returns the name of the resources as written in the XML. This undos the flattening of some
     * characters to '_' as done by aapt.
     * <p/>
     * The method is not guaranteed to be implemented on the IDE side. In such a case, the method
     * will return null.
     * @param type the type of the resource
     * @param name the name of the resource that may have been obtained from the R class.
     * @param isFramework whether the resource is a framework resource.
     *
     * @return the name as written in the XML or null if not implemented for the resource type.
     */
    public String getXmlName(ResourceType type, String name, boolean isFramework) {
        return null;
    }
}
