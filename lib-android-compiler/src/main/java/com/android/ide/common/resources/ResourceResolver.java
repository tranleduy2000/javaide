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

package com.android.ide.common.resources;

import static com.android.SdkConstants.ANDROID_STYLE_RESOURCE_PREFIX;
import static com.android.SdkConstants.PREFIX_ANDROID;
import static com.android.SdkConstants.PREFIX_RESOURCE_REF;
import static com.android.SdkConstants.REFERENCE_STYLE;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.ide.common.rendering.api.LayoutLog;
import com.android.ide.common.rendering.api.RenderResources;
import com.android.ide.common.rendering.api.ResourceValue;
import com.android.ide.common.rendering.api.StyleResourceValue;
import com.android.resources.ResourceType;
import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResourceResolver extends RenderResources {
    public static final String THEME_NAME = "Theme";
    public static final String THEME_NAME_DOT = "Theme.";
    public static final String XLIFF_NAMESPACE_PREFIX = "urn:oasis:names:tc:xliff:document:";
    public static final String XLIFF_G_TAG = "g";
    public static final String ATTR_EXAMPLE = "example";

    /**
     * Number of indirections we'll follow for resource resolution before assuming there
     * is a cyclic dependency error in the input
     */
    public static final int MAX_RESOURCE_INDIRECTION = 50;

    private final Map<ResourceType, Map<String, ResourceValue>> mProjectResources;
    private final Map<ResourceType, Map<String, ResourceValue>> mFrameworkResources;
    private final Map<StyleResourceValue, StyleResourceValue> mStyleInheritanceMap =
        new HashMap<StyleResourceValue, StyleResourceValue>();
    private StyleResourceValue mDefaultTheme;
    // The resources should be searched in all the themes in the list in order.
    private final List<StyleResourceValue> mThemes;
    private FrameworkResourceIdProvider mFrameworkProvider;
    private LayoutLog mLogger;
    private String mThemeName;
    private boolean mIsProjectTheme;
    // AAPT flattens the names by converting '.', '-' and ':' to '_'. These maps undo the
    // flattening. We prefer lazy initialization of these maps since they are not used in many
    // applications.
    @Nullable
    private Map<String, String> mReverseFrameworkStyles;
    @Nullable
    private Map<String, String> mReverseProjectStyles;

    private ResourceResolver(
            Map<ResourceType, Map<String, ResourceValue>> projectResources,
            Map<ResourceType, Map<String, ResourceValue>> frameworkResources,
            String themeName, boolean isProjectTheme) {
        mProjectResources = projectResources;
        mFrameworkResources = frameworkResources;
        mThemeName = themeName;
        mIsProjectTheme = isProjectTheme;
        mThemes = new LinkedList<StyleResourceValue>();
    }

    /**
     * Creates a new {@link ResourceResolver} object.
     *
     * @param projectResources the project resources.
     * @param frameworkResources the framework resources.
     * @param themeName the name of the current theme.
     * @param isProjectTheme Is this a project theme?
     * @return a new {@link ResourceResolver}
     */
    public static ResourceResolver create(
            Map<ResourceType, Map<String, ResourceValue>> projectResources,
            Map<ResourceType, Map<String, ResourceValue>> frameworkResources,
            String themeName, boolean isProjectTheme) {

        ResourceResolver resolver = new ResourceResolver(projectResources, frameworkResources,
                themeName, isProjectTheme);
        resolver.computeStyleMaps();

        return resolver;
    }

    /**
     * Sets up the light and dark default styles with the given concrete styles. This is used if we
     * want to override the defaults configured in the framework for this particular platform.
     */
    public void setDeviceDefaults(@Nullable String lightStyle, @Nullable String darkStyle) {
        if (darkStyle != null) {
            replace("Theme.DeviceDefault", darkStyle);
        }
        if (lightStyle != null && replace("Theme.DeviceDefault.Light", lightStyle)) {
            replace("Theme.DeviceDefault.Light.DarkActionBar", lightStyle + ".DarkActionBar");
        }
    }

    private boolean replace(String fromStyleName, String toStyleName) {
        Map<String, ResourceValue> map = mFrameworkResources.get(ResourceType.STYLE);
        if (map != null) {
            ResourceValue from = map.get(fromStyleName);
            if (from instanceof StyleResourceValue) {
                ResourceValue to = map.get(toStyleName);
                if (to instanceof StyleResourceValue) {
                    mStyleInheritanceMap.put((StyleResourceValue)from, (StyleResourceValue)to);
                    return true;
                }
            }
        }
        return false;
    }

    // ---- Methods to help dealing with older LayoutLibs.

    public String getThemeName() {
        return mThemeName;
    }

    public boolean isProjectTheme() {
        return mIsProjectTheme;
    }

    public Map<ResourceType, Map<String, ResourceValue>> getProjectResources() {
        return mProjectResources;
    }

    public Map<ResourceType, Map<String, ResourceValue>> getFrameworkResources() {
        return mFrameworkResources;
    }

    // ---- RenderResources Methods

    @Override
    public void setFrameworkResourceIdProvider(FrameworkResourceIdProvider provider) {
        mFrameworkProvider = provider;
    }

    @Override
    public void setLogger(LayoutLog logger) {
        mLogger = logger;
    }

    @Override
    public StyleResourceValue getDefaultTheme() {
        return mDefaultTheme;
    }

    @Override
    public void applyStyle(StyleResourceValue theme, boolean useAsPrimary) {
        if (theme == null) {
            return;
        }
        if (useAsPrimary) {
            mThemes.add(0, theme);
        } else {
            mThemes.add(theme);
        }
    }

    @Override
    public void clearStyles() {
        mThemes.clear();
        mThemes.add(mDefaultTheme);
    }

    @Override
    public List<StyleResourceValue> getAllThemes() {
        return mThemes;
    }

  @Override
    public StyleResourceValue getTheme(String name, boolean frameworkTheme) {
        ResourceValue theme;

        if (frameworkTheme) {
            Map<String, ResourceValue> frameworkStyleMap = mFrameworkResources.get(
                    ResourceType.STYLE);
            theme = frameworkStyleMap.get(name);
        } else {
            Map<String, ResourceValue> projectStyleMap = mProjectResources.get(ResourceType.STYLE);
            theme = projectStyleMap.get(name);
        }

        if (theme instanceof StyleResourceValue) {
            return (StyleResourceValue) theme;
        }

        return null;
    }

    @Override
    public boolean themeIsParentOf(StyleResourceValue parentTheme, StyleResourceValue childTheme) {
        do {
            childTheme = mStyleInheritanceMap.get(childTheme);
            if (childTheme == null) {
                return false;
            } else if (childTheme == parentTheme) {
                return true;
            }
        } while (true);
    }

    @Override
    public ResourceValue getFrameworkResource(ResourceType resourceType, String resourceName) {
        return getResource(resourceType, resourceName, mFrameworkResources);
    }

    @Override
    public ResourceValue getProjectResource(ResourceType resourceType, String resourceName) {
        return getResource(resourceType, resourceName, mProjectResources);
    }

    @SuppressWarnings("deprecation") // Required to support older layoutlib clients
    @Override
    @Deprecated
    public ResourceValue findItemInStyle(StyleResourceValue style, String attrName) {
        // this method is deprecated because it doesn't know about the namespace of the
        // attribute so we search for the project namespace first and then in the
        // android namespace if needed.
        ResourceValue item = findItemInStyle(style, attrName, false);
        if (item == null) {
            item = findItemInStyle(style, attrName, true);
        }

        return item;
    }

    @Override
    public ResourceValue findItemInStyle(StyleResourceValue style, String itemName,
            boolean isFrameworkAttr) {
        return findItemInStyle(style, itemName, isFrameworkAttr, 0);
    }

    private ResourceValue findItemInStyle(StyleResourceValue style, String itemName,
                                          boolean isFrameworkAttr, int depth) {
        ResourceValue item = style.getItem(itemName, isFrameworkAttr);

        // if we didn't find it, we look in the parent style (if applicable)
        //noinspection VariableNotUsedInsideIf
        if (item == null) {
            StyleResourceValue parentStyle = mStyleInheritanceMap.get(style);
            if (parentStyle != null) {
                if (depth >= MAX_RESOURCE_INDIRECTION) {
                    if (mLogger != null) {
                        mLogger.error(LayoutLog.TAG_BROKEN,
                                String.format("Cyclic style parent definitions: %1$s",
                                        computeCyclicStyleChain(style)),
                                null);
                    }

                    return null;
                }

                return findItemInStyle(parentStyle, itemName, isFrameworkAttr, depth + 1);
            }
        }

        return item;
    }

    private String computeCyclicStyleChain(StyleResourceValue style) {
        StringBuilder sb = new StringBuilder(100);
        appendStyleParents(style, new HashSet<StyleResourceValue>(), 0, sb);
        return sb.toString();
    }

    private void appendStyleParents(StyleResourceValue style, Set<StyleResourceValue> seen,
            int depth, StringBuilder sb) {
        if (depth >= MAX_RESOURCE_INDIRECTION) {
            sb.append("...");
            return;
        }

        boolean haveSeen = seen.contains(style);
        seen.add(style);

        sb.append('"');
        if (style.isFramework()) {
            sb.append(PREFIX_ANDROID);
        }
        sb.append(style.getName());
        sb.append('"');

        if (haveSeen) {
            return;
        }

        StyleResourceValue parentStyle = mStyleInheritanceMap.get(style);
        if (parentStyle != null) {
            if (style.getParentStyle() != null) {
                sb.append(" specifies parent ");
            } else {
                sb.append(" implies parent ");
            }

            appendStyleParents(parentStyle, seen, depth + 1, sb);
        }
    }

    @Override
    public ResourceValue findResValue(String reference, boolean forceFrameworkOnly) {
        if (reference == null) {
            return null;
        }

        ResourceUrl resource = ResourceUrl.parse(reference);
        if (resource != null && resource.hasValidName()) {
            if (resource.theme) {
                // no theme? no need to go further!
                if (mDefaultTheme == null) {
                    return null;
                }

                if (resource.type != ResourceType.ATTR) {
                    // At this time, no support for ?type/name where type is not "attr"
                    return null;
                }

                // Now look for the item in the theme, starting with the current one.
                return findItemInTheme(resource.name, forceFrameworkOnly || resource.framework);
            } else {
                return findResValue(resource, forceFrameworkOnly);
            }
        }

        // Looks like the value didn't reference anything. Return null.
        return null;
    }

    @Override
    public ResourceValue resolveValue(ResourceType type, String name, String value,
            boolean isFrameworkValue) {
        if (value == null) {
            return null;
        }

        // get the ResourceValue referenced by this value
        ResourceValue resValue = findResValue(value, isFrameworkValue);

        // if resValue is null, but value is not null, this means it was not a reference.
        // we return the name/value wrapper in a ResourceValue. the isFramework flag doesn't
        // matter.
        if (resValue == null) {
            return new ResourceValue(type, name, value, isFrameworkValue);
        }

        // we resolved a first reference, but we need to make sure this isn't a reference also.
        return resolveResValue(resValue);
    }

    @Override
    public ResourceValue resolveResValue(ResourceValue resValue) {
        return resolveResValue(resValue, 0);
    }

    private ResourceValue resolveResValue(ResourceValue resValue, int depth) {
        if (resValue == null) {
            return null;
        }

        // if the resource value is null, we simply return it.
        String value = resValue.getValue();
        if (value == null) {
            return resValue;
        }

        // else attempt to find another ResourceValue referenced by this one.
        ResourceValue resolvedResValue = findResValue(value, resValue.isFramework());

        // if the value did not reference anything, then we simply return the input value
        if (resolvedResValue == null) {
            return resValue;
        }

        // detect potential loop due to mishandled namespace in attributes
        if (resValue == resolvedResValue || depth >= MAX_RESOURCE_INDIRECTION) {
            if (mLogger != null) {
                mLogger.error(LayoutLog.TAG_BROKEN,
                        String.format("Potential stack overflow trying to resolve '%s': cyclic resource definitions? Render may not be accurate.", value),
                        null);
            }
            return resValue;
        }

        // otherwise, we attempt to resolve this new value as well
        return resolveResValue(resolvedResValue, depth + 1);
    }

    // ---- Private helper methods.

    /**
     * Searches for, and returns a {@link ResourceValue} by its parsed reference.
     * @param resource the parsed resource
     * @param forceFramework if <code>true</code>, the method does not search in the
     * project resources
     */
    private ResourceValue findResValue(ResourceUrl resource, boolean forceFramework) {
        // map of ResourceValue for the given type
        Map<String, ResourceValue> typeMap;
        ResourceType resType = resource.type;
        String resName = resource.name;
        boolean isFramework = forceFramework || resource.framework;

        if (!isFramework) {
            typeMap = mProjectResources.get(resType);
            ResourceValue item = typeMap.get(resName);
            if (item != null) {
                return item;
            }
        } else {
            typeMap = mFrameworkResources.get(resType);
            if (typeMap != null) {
                ResourceValue item = typeMap.get(resName);
                if (item != null) {
                    return item;
                }
            }

            // if it was not found and the type is an id, it is possible that the ID was
            // generated dynamically when compiling the framework resources.
            // Look for it in the R map.
            if (mFrameworkProvider != null && resType == ResourceType.ID) {
                if (mFrameworkProvider.getId(resType, resName) != null) {
                    return new ResourceValue(resType, resName, true);
                }
            }
        }

      // didn't find the resource anywhere.
        if (!resource.create && mLogger != null) {
            mLogger.warning(LayoutLog.TAG_RESOURCES_RESOLVE,
                    "Couldn't resolve resource @" +
                    (isFramework ? "android:" : "") + resType + "/" + resName,
                    new ResourceValue(resType, resName, isFramework));
        }
        return null;
    }

    private ResourceValue getResource(ResourceType resourceType, String resourceName,
            Map<ResourceType, Map<String, ResourceValue>> resourceRepository) {
        Map<String, ResourceValue> typeMap = resourceRepository.get(resourceType);
        if (typeMap != null) {
            ResourceValue item = typeMap.get(resourceName);
            if (item != null) {
                item = resolveResValue(item);
                return item;
            }
        }

        // didn't find the resource anywhere.
        return null;
    }

    /**
     * Compute style information from the given list of style for the project and framework.
     */
    private void computeStyleMaps() {
        Map<String, ResourceValue> projectStyleMap = mProjectResources.get(ResourceType.STYLE);
        Map<String, ResourceValue> frameworkStyleMap = mFrameworkResources.get(ResourceType.STYLE);

        // first, get the theme
        ResourceValue theme = null;

        // project theme names have been prepended with a *
        if (mIsProjectTheme) {
            if (projectStyleMap != null) {
                theme = projectStyleMap.get(mThemeName);
            }
        } else {
            if (frameworkStyleMap != null) {
                theme = frameworkStyleMap.get(mThemeName);
            }
        }

        if (theme instanceof StyleResourceValue) {
            // compute the inheritance map for both the project and framework styles
            computeStyleInheritance(projectStyleMap.values(), projectStyleMap,
                    frameworkStyleMap);

            // Compute the style inheritance for the framework styles/themes.
            // Since, for those, the style parent values do not contain 'android:'
            // we want to force looking in the framework style only to avoid using
            // similarly named styles from the project.
            // To do this, we pass null in lieu of the project style map.
            if (frameworkStyleMap != null) {
                computeStyleInheritance(frameworkStyleMap.values(), null /*inProjectStyleMap */,
                        frameworkStyleMap);
            }

            mDefaultTheme = (StyleResourceValue) theme;
            mThemes.clear();
            mThemes.add(mDefaultTheme);
        }
    }

    /**
     * Compute the parent style for all the styles in a given list.
     * @param styles the styles for which we compute the parent.
     * @param inProjectStyleMap the map of project styles.
     * @param inFrameworkStyleMap the map of framework styles.
     */
    private void computeStyleInheritance(Collection<ResourceValue> styles,
            Map<String, ResourceValue> inProjectStyleMap,
            Map<String, ResourceValue> inFrameworkStyleMap) {
        for (ResourceValue value : styles) {
            if (value instanceof StyleResourceValue) {
                StyleResourceValue style = (StyleResourceValue)value;

                // first look for a specified parent.
                String parentName = style.getParentStyle();

                // no specified parent? try to infer it from the name of the style.
                if (parentName == null) {
                    parentName = getParentName(value.getName());
                }

                if (parentName != null) {
                    StyleResourceValue parentStyle = getStyle(parentName, inProjectStyleMap,
                            inFrameworkStyleMap);

                    if (parentStyle != null) {
                        mStyleInheritanceMap.put(style, parentStyle);
                    }
                }
            }
        }
    }

    /**
     * Computes the name of the parent style, or <code>null</code> if the style is a root style.
     */
    private static String getParentName(String styleName) {
        int index = styleName.lastIndexOf('.');
        if (index != -1) {
            return styleName.substring(0, index);
        }

        return null;
    }

    @Override
    @Nullable
    public StyleResourceValue getParent(@NonNull StyleResourceValue style) {
        return mStyleInheritanceMap.get(style);
    }

    @Override
    @Nullable
    public StyleResourceValue getStyle(@NonNull String styleName, boolean isFramework) {
        ResourceValue res;
        Map<String, ResourceValue> styleMap;

        // First check if we can find the style directly.
        if (isFramework) {
            styleMap = mFrameworkResources.get(ResourceType.STYLE);
        } else {
            styleMap = mProjectResources.get(ResourceType.STYLE);
        }
        res = getStyleFromMap(styleMap, styleName);
        if (res != null) {
            // If the obtained resource is not StyleResourceValue, return null.
            return res instanceof StyleResourceValue ? (StyleResourceValue) res : null;
        }

        // We cannot find the style directly. The style name may have been flattened by AAPT for use
        // in the R class. Try and obtain the original name.
        String xmlStyleName = getReverseStyleMap(isFramework)
                .get(getNormalizedStyleName(styleName));
        if (!styleName.equals(xmlStyleName)) {
            res = getStyleFromMap(styleMap, xmlStyleName);
        }
        return res instanceof StyleResourceValue ? (StyleResourceValue) res : null;
    }

    @Override
    @Nullable
    public String getXmlName(@NonNull ResourceType type, @NonNull String name,
            boolean isFramework) {
        if (type != ResourceType.STYLE) {
            // The method is currently implemented for styles only.
            return null;
        }
        Map<String, String> reverseStyles;
        reverseStyles = getReverseStyleMap(isFramework);
        return reverseStyles.get(name);
    }

    /**
     * Returns the reverse style map using the appropriate resources. It also initializes the map if
     * it hasn't been initialized yet.
     */
    private Map<String, String> getReverseStyleMap(boolean isFramework) {
        if (isFramework) {
            // The reverse style map may need to be initialized.
            if (mReverseFrameworkStyles == null) {
                Map<String, ResourceValue> styleMap = mFrameworkResources.get(ResourceType.STYLE);
                mReverseFrameworkStyles = createReverseStyleMap(styleMap.keySet());
            }
            return mReverseFrameworkStyles;
        } else {
            if (mReverseProjectStyles == null) {
                Map<String, ResourceValue> styleMap = mProjectResources.get(ResourceType.STYLE);
                mReverseProjectStyles = createReverseStyleMap(styleMap.keySet());
            }
            return mReverseProjectStyles;
        }
    }

    /**
     * Create a map from the normalized form of the style names in {@code styles} to the original
     * style name.
     *
     * @see #getNormalizedStyleName(String)
     */
    private static Map<String, String> createReverseStyleMap(@NonNull Set<String> styles) {
        Map<String, String> reverseStyles = Maps.newHashMapWithExpectedSize(styles.size());
        for (String style : styles) {
            reverseStyles.put(getNormalizedStyleName(style), style);
        }
        return reverseStyles;
    }

    /**
     * Flatten the styleName like AAPT by replacing dots, dashes and colons with underscores.
     */
    @NonNull
    private static String getNormalizedStyleName(@NonNull String styleName) {
        return styleName.replace('.', '_').replace('-', '_').replace(':', '_');
    }

    /**
     * Search for the style in the given map and log an error if the obtained resource is not
     * {@link StyleResourceValue}.
     *
     * @return The {@link ResourceValue} found in the map.
     */
    @Nullable
    private ResourceValue getStyleFromMap(@NonNull Map<String, ResourceValue> styleMap,
            @NonNull String styleName) {
        ResourceValue res;
        res = styleMap.get(styleName);
        if (res != null) {
            if (!(res instanceof StyleResourceValue) && mLogger != null) {
                mLogger.error(null, String.format(
                                "Style %1$s is not of type STYLE (instead %2$s)",
                                styleName, res.getResourceType().toString()),
                        null);
            }
        }
        return res;
    }

    /**
     * Searches for and returns the {@link StyleResourceValue} from a given name.
     * <p/>The format of the name can be:
     * <ul>
     * <li>[android:]&lt;name&gt;</li>
     * <li>[android:]style/&lt;name&gt;</li>
     * <li>@[android:]style/&lt;name&gt;</li>
     * </ul>
     * @param parentName the name of the style.
     * @param inProjectStyleMap the project style map. Can be <code>null</code>
     * @param inFrameworkStyleMap the framework style map.
     * @return The matching {@link StyleResourceValue} object or <code>null</code> if not found.
     */
    private StyleResourceValue getStyle(String parentName,
            Map<String, ResourceValue> inProjectStyleMap,
            Map<String, ResourceValue> inFrameworkStyleMap) {
        boolean frameworkOnly = false;

        String name = parentName;

        // remove the useless @ if it's there
        if (name.startsWith(PREFIX_RESOURCE_REF)) {
            name = name.substring(PREFIX_RESOURCE_REF.length());
        }

        // check for framework identifier.
        if (name.startsWith(PREFIX_ANDROID)) {
            frameworkOnly = true;
            name = name.substring(PREFIX_ANDROID.length());
        }

        // at this point we could have the format <type>/<name>. we want only the name as long as
        // the type is style.
        if (name.startsWith(REFERENCE_STYLE)) {
            name = name.substring(REFERENCE_STYLE.length());
        } else if (name.indexOf('/') != -1) {
            return null;
        }

        ResourceValue parent = null;

        // if allowed, search in the project resources.
        if (!frameworkOnly && inProjectStyleMap != null) {
            parent = inProjectStyleMap.get(name);
        }

        // if not found, then look in the framework resources.
        if (parent == null) {
            if (inFrameworkStyleMap == null) {
                return null;
            }
            parent = inFrameworkStyleMap.get(name);
        }

        // make sure the result is the proper class type and return it.
        if (parent instanceof StyleResourceValue) {
            return (StyleResourceValue)parent;
        }

        if (mLogger != null) {
            mLogger.error(LayoutLog.TAG_RESOURCES_RESOLVE,
                    String.format("Unable to resolve parent style name: %s", parentName),
                    null /*data*/);
        }

        return null;
    }

    /** Returns true if the given {@link ResourceValue} represents a theme */
    public boolean isTheme(
            @NonNull ResourceValue value,
            @Nullable Map<ResourceValue, Boolean> cache) {
        return isTheme(value, cache, 0);
    }

    private boolean isTheme(
            @NonNull ResourceValue value,
            @Nullable Map<ResourceValue, Boolean> cache,
            int depth) {
        if (cache != null) {
            Boolean known = cache.get(value);
            if (known != null) {
                return known;
            }
        }
        if (value instanceof StyleResourceValue) {
            StyleResourceValue srv = (StyleResourceValue) value;
            String name = srv.getName();
            if (srv.isFramework() && (name.equals(THEME_NAME) || name.startsWith(THEME_NAME_DOT))) {
                if (cache != null) {
                    cache.put(value, true);
                }
                return true;
            }

            StyleResourceValue parentStyle = mStyleInheritanceMap.get(srv);
            if (parentStyle != null) {
                if (depth >= MAX_RESOURCE_INDIRECTION) {
                    if (mLogger != null) {
                        mLogger.error(LayoutLog.TAG_BROKEN,
                                String.format("Cyclic style parent definitions: %1$s",
                                        computeCyclicStyleChain(srv)),
                                null);
                    }

                    return false;
                }

                boolean result = isTheme(parentStyle, cache, depth + 1);
                if (cache != null) {
                    cache.put(value, result);
                }
                return result;
            }
        }

        return false;
    }

    /**
     * Returns true if the given {@code themeStyle} extends the theme given by
     * {@code parentStyle}
     */
    public boolean themeExtends(@NonNull String parentStyle, @NonNull String themeStyle) {
        ResourceValue parentValue = findResValue(parentStyle,
                parentStyle.startsWith(ANDROID_STYLE_RESOURCE_PREFIX));
        if (parentValue instanceof StyleResourceValue) {
            ResourceValue themeValue = findResValue(themeStyle,
                    themeStyle.startsWith(ANDROID_STYLE_RESOURCE_PREFIX));
            if (themeValue == parentValue) {
                return true;
            }
            if (themeValue instanceof StyleResourceValue) {
                return themeIsParentOf((StyleResourceValue) parentValue,
                        (StyleResourceValue) themeValue);
            }
        }

        return false;
    }

    /**
     * Creates a new {@link ResourceResolver} which records all resource resolution
     * lookups into the given list. Note that it is the responsibility of the caller
     * to clear/reset the list between subsequent lookup operations.
     *
     * @param lookupChain the list to write resource lookups into
     * @return a new {@link ResourceResolver}
     */
    public ResourceResolver createRecorder(List<ResourceValue> lookupChain) {
        ResourceResolver resolver = new RecordingResourceResolver(
                lookupChain, mProjectResources, mFrameworkResources, mThemeName, mIsProjectTheme);
        resolver.mFrameworkProvider = mFrameworkProvider;
        resolver.mLogger = mLogger;
        resolver.mDefaultTheme = mDefaultTheme;
        resolver.mStyleInheritanceMap.putAll(mStyleInheritanceMap);
        resolver.mThemes.addAll(mThemes);
        return resolver;
    }

    private static class RecordingResourceResolver extends ResourceResolver {
        @NonNull private List<ResourceValue> mLookupChain;

        private RecordingResourceResolver(
                @NonNull List<ResourceValue> lookupChain,
                @NonNull Map<ResourceType, Map<String, ResourceValue>> projectResources,
                @NonNull Map<ResourceType, Map<String, ResourceValue>> frameworkResources,
                @NonNull String themeName, boolean isProjectTheme) {
            super(projectResources, frameworkResources, themeName, isProjectTheme);
            mLookupChain = lookupChain;
        }

        @Override
        public ResourceValue resolveResValue(ResourceValue resValue) {
            if (resValue != null) {
                mLookupChain.add(resValue);
            }

            return super.resolveResValue(resValue);
        }

        @Override
        public ResourceValue findResValue(String reference, boolean forceFrameworkOnly) {
            if (!mLookupChain.isEmpty() && reference.startsWith(PREFIX_RESOURCE_REF)) {
                ResourceValue prev = mLookupChain.get(mLookupChain.size() - 1);
                if (!reference.equals(prev.getValue())) {
                    ResourceValue next = new ResourceValue(prev.getResourceType(), prev.getName(),
                            prev.isFramework());
                    next.setValue(reference);
                    mLookupChain.add(next);
                }
            }

            ResourceValue resValue = super.findResValue(reference, forceFrameworkOnly);

            if (resValue != null) {
                mLookupChain.add(resValue);
            }

            return resValue;
        }

        @Override
        public ResourceValue findItemInStyle(StyleResourceValue style, String itemName,
                boolean isFrameworkAttr) {
            ResourceValue value = super.findItemInStyle(style, itemName, isFrameworkAttr);
            if (value != null) {
                mLookupChain.add(value);
            }
            return value;
        }

        @Override
        public ResourceValue findItemInTheme(String attrName, boolean isFrameworkAttr) {
            ResourceValue value = super.findItemInTheme(attrName, isFrameworkAttr);
            if (value != null) {
                mLookupChain.add(value);
            }
            return value;
        }

        @Override
        public ResourceValue resolveValue(ResourceType type, String name, String value,
                boolean isFrameworkValue) {
            ResourceValue resourceValue = super.resolveValue(type, name, value, isFrameworkValue);
            if (resourceValue != null) {
                mLookupChain.add(resourceValue);
            }
            return resourceValue;
        }
    }
}
