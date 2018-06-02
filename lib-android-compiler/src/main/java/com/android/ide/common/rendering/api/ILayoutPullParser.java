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

import org.xmlpull.v1.XmlPullParser;

/**
 * Extended version of {@link XmlPullParser} to use with
 * {@link Bridge#createSession(SessionParams)}
 */
public interface ILayoutPullParser extends XmlPullParser {

    /**
     * Returns a cookie for the current XML node.
     * <p/>This cookie will be passed back in the {@link ViewInfo} objects, allowing association
     * of a particular XML node with its result from the layout computation.
     *
     * @see ViewInfo#getCookie()
     */
    Object getViewCookie();

    /**
     * Returns a custom parser for the layout of the given name.
     * @param layoutName the name of the layout.
     * @return returns a custom parser or null if no custom parsers are needed.
     *
     * @deprecated use {@link LayoutlibCallback#getParser(String)} instead
     */
    @Deprecated
    ILayoutPullParser getParser(String layoutName);
}

