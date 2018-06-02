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

package com.android.ide.common.res2;

import static com.android.SdkConstants.ATTR_REF_PREFIX;
import static com.android.SdkConstants.PREFIX_RESOURCE_REF;
import static com.android.SdkConstants.PREFIX_THEME_REF;
import static com.android.SdkConstants.RESOURCE_CLZ_ATTR;
import static com.android.ide.common.resources.ResourceResolver.MAX_RESOURCE_INDIRECTION;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.ide.common.rendering.api.ResourceValue;
import com.android.ide.common.resources.ResourceUrl;
import com.android.ide.common.resources.configuration.Configurable;
import com.android.ide.common.resources.configuration.FolderConfiguration;
import com.android.ide.common.resources.configuration.LocaleQualifier;
import com.android.resources.ResourceType;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilderFactory;

public abstract class AbstractResourceRepository {

    private final boolean mFramework;

    private class RepositoryMerger implements MergeConsumer<ResourceItem> {

        @Override
        public void start(@NonNull DocumentBuilderFactory factory)
                throws ConsumerException {
        }

        @Override
        public void end() throws ConsumerException {
        }

        @Override
        public void addItem(@NonNull ResourceItem item) throws ConsumerException {
            if (item.isTouched()) {
                AbstractResourceRepository.this.addItem(item);
            }
        }

        @Override
        public void removeItem(@NonNull ResourceItem removedItem, @Nullable ResourceItem replacedBy)
                throws ConsumerException {
            AbstractResourceRepository.this.removeItem(removedItem);
        }

        @Override
        public boolean ignoreItemInMerge(ResourceItem item) {
            // we never ignore any item.
            return false;
        }
    }

    public AbstractResourceRepository(boolean isFramework) {
        mFramework = isFramework;
    }

    public boolean isFramework() {
        return mFramework;
    }

    @NonNull
    public MergeConsumer<ResourceItem> createMergeConsumer() {
        return new RepositoryMerger();
    }

    @NonNull
    protected abstract Map<ResourceType, ListMultimap<String, ResourceItem>> getMap();

    @Nullable
    protected abstract ListMultimap<String, ResourceItem> getMap(ResourceType type, boolean create);

    @NonNull
    protected ListMultimap<String, ResourceItem> getMap(ResourceType type) {
        //noinspection ConstantConditions
        return getMap(type, true); // Won't return null if create is false
    }

    @NonNull
    public Map<ResourceType, ListMultimap<String, ResourceItem>> getItems() {
        return getMap();
    }

    /** Lock used to protect map access */
    protected static final Object ITEM_MAP_LOCK = new Object();

    // TODO: Rename to getResourceItemList?
    @Nullable
    public List<ResourceItem> getResourceItem(@NonNull ResourceType resourceType,
            @NonNull String resourceName) {
        synchronized (ITEM_MAP_LOCK) {
            ListMultimap<String, ResourceItem> map = getMap(resourceType, false);

            if (map != null) {
                return map.get(resourceName);
            }
        }

        return null;
    }

    @NonNull
    public Collection<String> getItemsOfType(@NonNull ResourceType type) {
        synchronized (ITEM_MAP_LOCK) {
            Multimap<String, ResourceItem> map = getMap(type, false);
            if (map == null) {
                return Collections.emptyList();
            }
            return Collections.unmodifiableCollection(map.keySet());
        }
    }

    /**
     * Returns true if this resource repository contains a resource of the given
     * name.
     *
     * @param url the resource URL
     * @return true if the resource is known
     */
    public boolean hasResourceItem(@NonNull String url) {
        // Handle theme references
        if (url.startsWith(PREFIX_THEME_REF)) {
            String remainder = url.substring(PREFIX_THEME_REF.length());
            if (url.startsWith(ATTR_REF_PREFIX)) {
                url = PREFIX_RESOURCE_REF + url.substring(PREFIX_THEME_REF.length());
                return hasResourceItem(url);
            }
            int colon = url.indexOf(':');
            if (colon != -1) {
                // Convert from ?android:progressBarStyleBig to ?android:attr/progressBarStyleBig
                if (remainder.indexOf('/', colon) == -1) {
                    remainder = remainder.substring(0, colon) + RESOURCE_CLZ_ATTR + '/'
                            + remainder.substring(colon);
                }
                url = PREFIX_RESOURCE_REF + remainder;
                return hasResourceItem(url);
            } else {
                int slash = url.indexOf('/');
                if (slash == -1) {
                    url = PREFIX_RESOURCE_REF + RESOURCE_CLZ_ATTR + '/' + remainder;
                    return hasResourceItem(url);
                }
            }
        }

        if (!url.startsWith(PREFIX_RESOURCE_REF)) {
            return false;
        }

        assert url.startsWith("@") || url.startsWith("?") : url;

        int typeEnd = url.indexOf('/', 1);
        if (typeEnd != -1) {
            int nameBegin = typeEnd + 1;

            // Skip @ and @+
            int typeBegin = url.startsWith("@+") ? 2 : 1; //$NON-NLS-1$

            int colon = url.lastIndexOf(':', typeEnd);
            if (colon != -1) {
                typeBegin = colon + 1;
            }
            String typeName = url.substring(typeBegin, typeEnd);
            ResourceType type = ResourceType.getEnum(typeName);
            if (type != null) {
                String name = url.substring(nameBegin);
                return hasResourceItem(type, name);
            }
        }

        return false;
    }

    /**
     * Returns true if this resource repository contains a resource of the given
     * name.
     *
     * @param resourceType the type of resource to look up
     * @param resourceName the name of the resource
     * @return true if the resource is known
     */
    public boolean hasResourceItem(@NonNull ResourceType resourceType,
            @NonNull String resourceName) {
        synchronized (ITEM_MAP_LOCK) {
            ListMultimap<String, ResourceItem> map = getMap(resourceType, false);

            if (map != null) {
                List<ResourceItem> itemList = map.get(resourceName);
                return itemList != null && !itemList.isEmpty();
            }
        }

        return false;
    }

    /**
     * Returns whether the repository has resources of a given {@link ResourceType}.
     * @param resourceType the type of resource to check.
     * @return true if the repository contains resources of the given type, false otherwise.
     */
    public boolean hasResourcesOfType(@NonNull ResourceType resourceType) {
        synchronized (ITEM_MAP_LOCK) {
            ListMultimap<String, ResourceItem> map = getMap(resourceType, false);
            return map != null && !map.isEmpty();
        }
    }

    @NonNull
    public List<ResourceType> getAvailableResourceTypes() {
        synchronized (ITEM_MAP_LOCK) {
            return Lists.newArrayList(getMap().keySet());
        }
    }

    /**
     * Returns the {@link ResourceFile} matching the given name, {@link ResourceType} and
     * configuration.
     * <p/>
     * This only works with files generating one resource named after the file
     * (for instance, layouts, bitmap based drawable, xml, anims).
     *
     * @param name the resource name
     * @param type the folder type search for
     * @param config the folder configuration to match for
     * @return the matching file or <code>null</code> if no match was found.
     */
    @Nullable
    public ResourceFile getMatchingFile(
            @NonNull String name,
            @NonNull ResourceType type,
            @NonNull FolderConfiguration config) {
        List<ResourceFile> matchingFiles = getMatchingFiles(name, type, config);
        return matchingFiles.isEmpty() ? null : matchingFiles.get(0);
    }

    /**
     * Returns a list of {@link ResourceFile} matching the given name, {@link ResourceType} and
     * configuration. This ignores the qualifiers which are missing from the configuration.
     * <p/>
     * This only works with files generating one resource named after the file (for instance,
     * layouts, bitmap based drawable, xml, anims).
     *
     * @param name the resource name
     * @param type the folder type search for
     * @param config the folder configuration to match for
     *
     * @see #getMatchingFile(String, ResourceType, FolderConfiguration)
     */
    @NonNull
    public List<ResourceFile> getMatchingFiles(
            @NonNull String name,
            @NonNull ResourceType type,
            @NonNull FolderConfiguration config) {
        return getMatchingFiles(name, type, config, new HashSet<String>(), 0);
    }

    @NonNull
    private List<ResourceFile> getMatchingFiles(
            @NonNull String name,
            @NonNull ResourceType type,
            @NonNull FolderConfiguration config,
            @NonNull Set<String> seenNames,
            int depth) {
        assert !seenNames.contains(name);
        if (depth >= MAX_RESOURCE_INDIRECTION) {
            return Collections.emptyList();
        }
        List<ResourceFile> output;
        synchronized (ITEM_MAP_LOCK) {
            ListMultimap<String, ResourceItem> typeItems = getMap(type, false);
            if (typeItems == null) {
                return Collections.emptyList();
            }
            seenNames.add(name);
            output = new ArrayList<ResourceFile>();
            List<ResourceItem> matchingItems = typeItems.get(name);
            List<Configurable> matches = config.findMatchingConfigurables(matchingItems);
            for (Configurable conf : matches) {
                ResourceItem match = (ResourceItem) conf;
                // if match is an alias, check if the name is in seen names.
                ResourceValue resourceValue = match.getResourceValue(isFramework());
                if (resourceValue != null) {
                    String value = resourceValue.getValue();
                    if (value != null && value.startsWith(PREFIX_RESOURCE_REF)) {
                        ResourceUrl url = ResourceUrl.parse(value);
                        if (url != null && url.type == type && url.framework == isFramework()) {
                            if (!seenNames.contains(url.name)) {
                                // This resource alias needs to be resolved again.
                                output.addAll(getMatchingFiles(
                                        url.name, type, config, seenNames, depth + 1));
                            }
                            continue;
                        }
                    }
                }
                output.add(match.getSource());

            }
        }

        return output;
    }

    /**
     * Returns the resources values matching a given {@link FolderConfiguration}.
     *
     * @param referenceConfig the configuration that each value must match.
     * @return a map with guaranteed to contain an entry for each {@link ResourceType}
     */
    @NonNull
    public Map<ResourceType, Map<String, ResourceValue>> getConfiguredResources(
            @NonNull FolderConfiguration referenceConfig) {
        Map<ResourceType, Map<String, ResourceValue>> map = Maps.newEnumMap(ResourceType.class);

        synchronized (ITEM_MAP_LOCK) {
            Map<ResourceType, ListMultimap<String, ResourceItem>> itemMap = getMap();
            for (ResourceType key : ResourceType.values()) {
                // get the local results and put them in the map
                map.put(key, getConfiguredResources(itemMap, key, referenceConfig));
            }
        }

        return map;
    }

    /**
     * Returns a map of (resource name, resource value) for the given {@link ResourceType}.
     * <p/>The values returned are taken from the resource files best matching a given
     * {@link FolderConfiguration}.
     * @param type the type of the resources.
     * @param referenceConfig the configuration to best match.
     */
    @NonNull
    public Map<String, ResourceValue> getConfiguredResources(
            @NonNull ResourceType type,
            @NonNull FolderConfiguration referenceConfig) {
        return getConfiguredResources(getMap(), type, referenceConfig);
    }

    @NonNull
    public Map<String, ResourceValue> getConfiguredResources(
            @NonNull Map<ResourceType, ListMultimap<String, ResourceItem>> itemMap,
            @NonNull ResourceType type,
            @NonNull FolderConfiguration referenceConfig) {
        // get the resource item for the given type
        ListMultimap<String, ResourceItem> items = itemMap.get(type);
        if (items == null) {
            return Maps.newHashMap();
        }

        Set<String> keys = items.keySet();

        // create the map
        Map<String, ResourceValue> map = Maps.newHashMapWithExpectedSize(keys.size());

        for (String key : keys) {
            List<ResourceItem> keyItems = items.get(key);

            // look for the best match for the given configuration
            // the match has to be of type ResourceFile since that's what the input list contains
            ResourceItem match = (ResourceItem) referenceConfig.findMatchingConfigurable(keyItems);
            if (match != null) {
                ResourceValue value = match.getResourceValue(mFramework);
                if (value != null) {
                    map.put(match.getName(), value);
                }
            }
        }

        return map;
    }

    @Nullable
    public ResourceValue getConfiguredValue(
            @NonNull ResourceType type,
            @NonNull String name,
            @NonNull FolderConfiguration referenceConfig) {
        // get the resource item for the given type
        ListMultimap<String, ResourceItem> items = getMap(type, false);
        if (items == null) {
            return null;
        }

        List<ResourceItem> keyItems = items.get(name);
        if (keyItems == null) {
            return null;
        }

        // look for the best match for the given configuration
        // the match has to be of type ResourceFile since that's what the input list contains
        ResourceItem match = (ResourceItem) referenceConfig.findMatchingConfigurable(keyItems);
        return match != null ? match.getResourceValue(mFramework) : null;
    }

    private void addItem(@NonNull ResourceItem item) {
        synchronized (ITEM_MAP_LOCK) {
            ListMultimap<String, ResourceItem> map = getMap(item.getType());
            if (!map.containsValue(item)) {
                map.put(item.getName(), item);
            }
        }
    }

    private void removeItem(@NonNull ResourceItem removedItem) {
        synchronized (ITEM_MAP_LOCK) {
            Multimap<String, ResourceItem> map = getMap(removedItem.getType(), false);
            if (map != null) {
                map.remove(removedItem.getName(), removedItem);
            }
        }
    }

    /**
     * Returns the sorted list of languages used in the resources.
     */
    @NonNull
    public SortedSet<String> getLanguages() {
        SortedSet<String> set = new TreeSet<String>();

        // As an optimization we could just look for values since that's typically where
        // the languages are defined -- not on layouts, menus, etc -- especially if there
        // are no translations for it
        Set<String> qualifiers = Sets.newHashSet();

        synchronized (ITEM_MAP_LOCK) {
            for (ListMultimap<String, ResourceItem> map : getMap().values()) {
                for (ResourceItem item : map.values()) {
                    qualifiers.add(item.getQualifiers());
                }
            }
        }

        for (String s : qualifiers) {
            FolderConfiguration configuration = FolderConfiguration.getConfigForQualifierString(s);
            if (configuration != null) {
                LocaleQualifier locale = configuration.getLocaleQualifier();
                if (locale != null) {
                    set.add(locale.getLanguage());
                }
            }
        }

        return set;
    }

    /**
     * Returns the sorted list of languages used in the resources.
     */
    @NonNull
    public SortedSet<LocaleQualifier> getLocales() {
        SortedSet<LocaleQualifier> set = new TreeSet<LocaleQualifier>();

        // As an optimization we could just look for values since that's typically where
        // the languages are defined -- not on layouts, menus, etc -- especially if there
        // are no translations for it
        Set<String> qualifiers = Sets.newHashSet();

        synchronized (ITEM_MAP_LOCK) {
            for (ListMultimap<String, ResourceItem> map : getMap().values()) {
                for (ResourceItem item : map.values()) {
                    qualifiers.add(item.getQualifiers());
                }
            }
        }

        for (String s : qualifiers) {
            FolderConfiguration configuration = FolderConfiguration.getConfigForQualifierString(s);
            if (configuration != null) {
                LocaleQualifier locale = configuration.getLocaleQualifier();
                if (locale != null) {
                    set.add(locale);
                }
            }
        }

        return set;
    }

    /**
     * Returns the sorted list of regions used in the resources with the given language.
     * @param currentLanguage the current language the region must be associated with.
     */
    @NonNull
    public SortedSet<String> getRegions(@NonNull String currentLanguage) {
        SortedSet<String> set = new TreeSet<String>();

        // As an optimization we could just look for values since that's typically where
        // the languages are defined -- not on layouts, menus, etc -- especially if there
        // are no translations for it
        Set<String> qualifiers = Sets.newHashSet();
        synchronized (ITEM_MAP_LOCK) {
            for (ListMultimap<String, ResourceItem> map : getMap().values()) {
                for (ResourceItem item : map.values()) {
                    qualifiers.add(item.getQualifiers());
                }
            }
        }

        for (String s : qualifiers) {
            FolderConfiguration configuration = FolderConfiguration.getConfigForQualifierString(s);
            if (configuration != null) {
                LocaleQualifier locale = configuration.getLocaleQualifier();
                if (locale != null && locale.getRegion() != null
                        && locale.getLanguage().equals(currentLanguage)) {
                    set.add(locale.getRegion());
                }
            }
        }

        return set;
    }

    public void clear() {
        getMap().clear();
    }
}
