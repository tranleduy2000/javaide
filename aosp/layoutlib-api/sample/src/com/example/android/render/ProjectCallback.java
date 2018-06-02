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

package com.example.android.render;

import com.android.ide.common.rendering.api.AdapterBinding;
import com.android.ide.common.rendering.api.ILayoutPullParser;
import com.android.ide.common.rendering.api.IProjectCallback;
import com.android.ide.common.rendering.api.ResourceReference;
import com.android.ide.common.rendering.api.ResourceValue;
import com.android.resources.ResourceType;
import com.android.util.Pair;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple implementation of {@link IProjectCallback}. This is a very basic implementation that
 * cannot support custom classes. Look for the one in ADT for custom class support.
 *
 * Because there's no custom view support, the int to resource name is all dynamic instad of
 * looking up in the R.java class that was compiled.
 *
 */
public class ProjectCallback implements IProjectCallback {

    private Map<ResourceType, Map<String, Integer>> mIdMap =
            new HashMap<ResourceType, Map<String, Integer>>();
    private Map<Integer, Pair<ResourceType, String>> mReverseIdMap =
            new HashMap<Integer, Pair<ResourceType,String>>();

    public ProjectCallback() {

    }

    public AdapterBinding getAdapterBinding(ResourceReference adapterViewRef, Object adapterCookie,
            Object viewObject) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object getAdapterItemValue(ResourceReference adapterView, Object adapterCookie,
            ResourceReference itemRef, int fullPosition, int positionPerType,
            int fullParentPosition, int parentPositionPerType, ResourceReference viewRef,
            ViewAttribute viewAttribute, Object defaultValue) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getNamespace() {
        // no custom class == no custom attribute, this is not needed.
        return null;
    }

    public ILayoutPullParser getParser(String layoutName) {
        // don't support custom parser for included files.
        return null;
    }

    public ILayoutPullParser getParser(ResourceValue layoutResource) {
        // don't support custom parser for included files.
        return null;
    }

    public Integer getResourceId(ResourceType type, String name) {
        // since we don't have access to compiled id, generate one on the fly.
        Map<String, Integer> typeMap = mIdMap.get(type);
        if (typeMap == null) {
            typeMap = new HashMap<String, Integer>();
            mIdMap.put(type, typeMap);
        }

        Integer value = typeMap.get(name);
        if (value == null) {
            value = typeMap.size() + 1;
            typeMap.put(name, value);
            mReverseIdMap.put(value, Pair.of(type, name));
        }

        return value;
    }

    @SuppressWarnings("unchecked")
    public Object loadView(String name, Class[] constructorSignature, Object[] constructorArgs)
            throws ClassNotFoundException, Exception {
        // don't support custom views.
        return null;
    }

    public Pair<ResourceType, String> resolveResourceId(int id) {
        return mReverseIdMap.get(id);
    }

    public String resolveResourceId(int[] id) {
        // this is needed only when custom views have custom styleable
        return null;
    }

}
