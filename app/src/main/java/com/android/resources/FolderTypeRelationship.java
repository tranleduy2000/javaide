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


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class gives access to the bidirectional relationship between {@link ResourceType} and
 * {@link ResourceFolderType}.
 */
public final class FolderTypeRelationship {

    private final static Map<ResourceType, List<ResourceFolderType>> mTypeToFolderMap =
        new HashMap<ResourceType, List<ResourceFolderType>>();

    private final static Map<ResourceFolderType, List<ResourceType>> mFolderToTypeMap =
        new HashMap<ResourceFolderType, List<ResourceType>>();

    static {
        // generate the relationships in a temporary map
        add(ResourceType.ANIM, ResourceFolderType.ANIM);
        add(ResourceType.ANIMATOR, ResourceFolderType.ANIMATOR);
        add(ResourceType.ARRAY, ResourceFolderType.VALUES);
        add(ResourceType.ATTR, ResourceFolderType.VALUES);
        add(ResourceType.BOOL, ResourceFolderType.VALUES);
        add(ResourceType.COLOR, ResourceFolderType.VALUES);
        add(ResourceType.COLOR, ResourceFolderType.COLOR);
        add(ResourceType.DECLARE_STYLEABLE, ResourceFolderType.VALUES);
        add(ResourceType.DIMEN, ResourceFolderType.VALUES);
        add(ResourceType.DRAWABLE, ResourceFolderType.VALUES);
        add(ResourceType.DRAWABLE, ResourceFolderType.DRAWABLE);
        add(ResourceType.FRACTION, ResourceFolderType.VALUES);
        add(ResourceType.ID, ResourceFolderType.VALUES);
        add(ResourceType.INTEGER, ResourceFolderType.VALUES);
        add(ResourceType.INTERPOLATOR, ResourceFolderType.INTERPOLATOR);
        add(ResourceType.LAYOUT, ResourceFolderType.LAYOUT);
        add(ResourceType.ID, ResourceFolderType.LAYOUT);
        add(ResourceType.MENU, ResourceFolderType.MENU);
        add(ResourceType.ID, ResourceFolderType.MENU);
        add(ResourceType.MIPMAP, ResourceFolderType.MIPMAP);
        add(ResourceType.PLURALS, ResourceFolderType.VALUES);
        add(ResourceType.PUBLIC, ResourceFolderType.VALUES);
        add(ResourceType.RAW, ResourceFolderType.RAW);
        add(ResourceType.STRING, ResourceFolderType.VALUES);
        add(ResourceType.STYLE, ResourceFolderType.VALUES);
        add(ResourceType.STYLEABLE, ResourceFolderType.VALUES);
        add(ResourceType.XML, ResourceFolderType.XML);

        makeSafe();
    }

    /**
     * Returns a list of {@link ResourceType}s that can be generated from files inside a folder
     * of the specified type.
     * @param folderType The folder type.
     * @return a list of {@link ResourceType}, possibly empty but never null.
     */
    public static List<ResourceType> getRelatedResourceTypes(ResourceFolderType folderType) {
        List<ResourceType> list = mFolderToTypeMap.get(folderType);
        if (list != null) {
            return list;
        }

        return Collections.emptyList();
    }

    /**
     * Returns a list of {@link ResourceFolderType} that can contain files generating resources
     * of the specified type.
     * @param resType the type of resource.
     * @return a list of {@link ResourceFolderType}, possibly empty but never null.
     */
    public static List<ResourceFolderType> getRelatedFolders(ResourceType resType) {
        List<ResourceFolderType> list = mTypeToFolderMap.get(resType);
        if (list != null) {
            return list;
        }

        return Collections.emptyList();
    }

    /**
     * Returns true if the {@link ResourceType} and the {@link ResourceFolderType} values match.
     * @param resType the resource type.
     * @param folderType the folder type.
     * @return true if files inside the folder of the specified {@link ResourceFolderType}
     * could generate a resource of the specified {@link ResourceType}
     */
    public static boolean match(ResourceType resType, ResourceFolderType folderType) {
        List<ResourceFolderType> list = mTypeToFolderMap.get(resType);

        if (list != null) {
            return list.contains(folderType);
        }

        return false;
    }

    /**
     * Adds a {@link ResourceType} - {@link ResourceFolderType} relationship. this indicates that
     * a file in the folder can generate a resource of the specified type.
     * @param type The resourceType
     * @param folder The {@link ResourceFolderType}
     */
    private static void add(ResourceType type, ResourceFolderType folder) {
        // first we add the folder to the list associated with the type.
        List<ResourceFolderType> folderList = mTypeToFolderMap.get(type);
        if (folderList == null) {
            folderList = new ArrayList<ResourceFolderType>();
            mTypeToFolderMap.put(type, folderList);
        }
        if (folderList.indexOf(folder) == -1) {
            folderList.add(folder);
        }

        // now we add the type to the list associated with the folder.
        List<ResourceType> typeList = mFolderToTypeMap.get(folder);
        if (typeList == null) {
            typeList = new ArrayList<ResourceType>();
            mFolderToTypeMap.put(folder, typeList);
        }
        if (typeList.indexOf(type) == -1) {
            typeList.add(type);
        }
    }

    /**
     * Makes the maps safe by replacing the current list values with unmodifiable lists.
     */
    private static void makeSafe() {
        for (ResourceType type : ResourceType.values()) {
            List<ResourceFolderType> list = mTypeToFolderMap.get(type);
            if (list != null) {
                // replace with a unmodifiable list wrapper around the current list.
                mTypeToFolderMap.put(type, Collections.unmodifiableList(list));
            }
        }

        for (ResourceFolderType folder : ResourceFolderType.values()) {
            List<ResourceType> list = mFolderToTypeMap.get(folder);
            if (list != null) {
                // replace with a unmodifiable list wrapper around the current list.
                mFolderToTypeMap.put(folder, Collections.unmodifiableList(list));
            }
        }
    }
}
