/*
 * Copyright (C) 2015 The Android Open Source Project
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

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Callback used by LayoutLib to create {@link XmlPullParser}s.
 */
public abstract class ParserFactory {
    /**
     * Creates a new XmlPullParser with an optional display name.
     *
     * @param debugName an optional name to aid with debugging.
     * @since API 15
     */
    @NonNull
    public XmlPullParser createParser(@Nullable String debugName) throws XmlPullParserException {
        throw new UnsupportedOperationException("createNewParser not supported.");
    }
}
