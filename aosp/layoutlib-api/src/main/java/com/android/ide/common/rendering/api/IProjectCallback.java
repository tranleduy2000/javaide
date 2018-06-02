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

import com.android.resources.ResourceType;
import com.android.util.Pair;

import java.net.URL;

/**
 * Callback for project information needed by the Layout Library.
 * Classes implementing this interface provide methods giving access to some project data, like
 * resource resolution, namespace information, and instantiation of custom view.
 * @deprecated use {@link LayoutlibCallback}
 */
@Deprecated
public interface IProjectCallback {

    enum ViewAttribute {
        TEXT(String.class),
        IS_CHECKED(Boolean.class),
        SRC(URL.class),
        COLOR(Integer.class);

        private final Class<?> mClass;

        ViewAttribute(Class<?> theClass) {
            mClass = theClass;
        }

        public Class<?> getAttributeClass() {
            return mClass;
        }
    }

    /**
     * Loads a custom class with the given constructor signature and arguments.
     * <p/>
     * Despite the name, the method is used not just for views (android.view.View),
     * but potentially any class in the project's namespace. However, when the
     * method is used for loading non-view classes the error messages reported may
     * not be ideal, since the the IDE may assume those classes to be a view and try
     * to use a different constructor or replace it with a MockView.
     * <p/>
     * This is done so that LayoutLib can continue to work on older versions of the IDE.
     * Newer versions of LayoutLib should call {@link
     * LayoutlibCallback#loadClass(String, Class[], Object[])} in such a case.
     *
     * @param name                 The fully qualified name of the class.
     * @param constructorSignature The signature of the class to use
     * @param constructorArgs      The arguments to use on the constructor
     * @return A newly instantiated object.
     */
    Object loadView(String name, Class[] constructorSignature, Object[] constructorArgs)
            throws Exception;

    /**
     * Returns the namespace of the application.
     * <p/>This lets the Layout Lib load custom attributes for custom views.
     */
    String getNamespace();

    /**
     * Resolves the id of a resource Id.
     * <p/>The resource id is the value of a <code>R.&lt;type&gt;.&lt;name&gt;</code>, and
     * this method will return both the type and name of the resource.
     * @param id the Id to resolve.
     * @return a Pair of {@link ResourceType} and resource name, or null if the id
     *     does not match any resource.
     */
    @SuppressWarnings("deprecation")
    Pair<ResourceType, String> resolveResourceId(int id);

    /**
     * Resolves the id of a resource Id of type int[]
     * <p/>The resource id is the value of a R.styleable.&lt;name&gt;, and this method will
     * return the name of the resource.
     * @param id the Id to resolve.
     * @return the name of the resource or <code>null</code> if not found.
     */
    String resolveResourceId(int[] id);

    /**
     * Returns the id of a resource.
     * <p/>The provided type and name must match an existing constant defined as
     * <code>R.&lt;type&gt;.&lt;name&gt;</code>.
     * @param type the type of the resource
     * @param name the name of the resource
     * @return an Integer containing the resource Id, or <code>null</code> if not found.
     */
    Integer getResourceId(ResourceType type, String name);

    /**
     * Returns a custom parser for the layout of the given name.
     * @param layoutName the name of the layout.
     * @return returns a custom parser or null if no custom parsers are needed.
     * @deprecated This is replaced by {@link #getParser(ResourceValue)} but older version
     * of the layoutlib (before API7) will still call this method.
     */
    @Deprecated
    ILayoutPullParser getParser(String layoutName);

    /**
     * Returns a custom parser for a given layout.
     * @param layoutResource The layout.
     * @return returns a custom parser or null if no custom parsers are needed.
     */
    ILayoutPullParser getParser(ResourceValue layoutResource);

    /**
     * Returns the value of an item used by an adapter.
     * @param adapterView The {@link ResourceReference} for the adapter view info.
     * @param adapterCookie the view cookie for this particular view.
     * @param itemRef the {@link ResourceReference} for the layout used by the adapter item.
     * @param fullPosition the position of the item in the full list.
     * @param positionPerType the position of the item if only items of the same type are
     *     considered. If there is only one type of items, this is the same as
     *     <var>fullPosition</var>.
     * @param fullParentPosition the full position of the item's parent. This is only
     *     valid if the adapter view is an ExpandableListView.
     * @param parentPositionPerType the position of the parent's item, only considering items
     *     of the same type. This is only valid if the adapter view is an ExpandableListView.
     *     If there is only one type of items, this is the same as <var>fullParentPosition</var>.
     * @param viewRef The {@link ResourceReference} for the view we're trying to fill.
     * @param viewAttribute the attribute being queried.
     * @param defaultValue the default value for this attribute. The object class matches the
     *      class associated with the {@link ViewAttribute}.
     * @return the item value or null if there's no value.
     *
     * @see ViewAttribute#getAttributeClass()
     */
    Object getAdapterItemValue(ResourceReference adapterView, Object adapterCookie,
            ResourceReference itemRef,
            int fullPosition, int positionPerType,
            int fullParentPosition, int parentPositionPerType,
            ResourceReference viewRef, ViewAttribute viewAttribute, Object defaultValue);

    /**
     * Returns an adapter binding for a given adapter view.
     * This is only called if {@link SessionParams} does not have an {@link AdapterBinding} for
     * the given {@link ResourceReference} already.
     *
     * @param adapterViewRef the reference of adapter view to return the adapter binding for.
     * @param adapterCookie the view cookie for this particular view.
     * @param viewObject the view object for the adapter.
     * @return an adapter binding for the given view or null if there's no data.
     */
    AdapterBinding getAdapterBinding(ResourceReference adapterViewRef, Object adapterCookie,
            Object viewObject);

    /**
     * Returns a callback for Action Bar information needed by the Layout Library. The callback
     * provides information like the menus to add to the Action Bar.
     *
     * @since API 11
     */
    ActionBarCallback getActionBarCallback();
}
