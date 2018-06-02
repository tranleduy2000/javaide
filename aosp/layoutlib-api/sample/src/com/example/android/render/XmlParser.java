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

import com.android.ide.common.rendering.api.ILayoutPullParser;
import com.android.ide.common.rendering.api.IProjectCallback;

import org.kxml2.io.KXmlParser;

/**
 * KXml-based parser that implements {@link ILayoutPullParser}.
 *
 */
public class XmlParser extends KXmlParser implements ILayoutPullParser {

    /**
     * @deprecated {@link IProjectCallback} replaces this.
     */
    @Deprecated
    public ILayoutPullParser getParser(String layoutName) {
        return null;
    }

    public Object getViewCookie() {
        return null;
    }
}
