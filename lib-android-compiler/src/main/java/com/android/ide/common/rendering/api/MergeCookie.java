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

/**
 * Special wrapper class used in special case for {@link ILayoutPullParser#getViewCookie()}.
 * <p/>
 * When an {@code include} tag points to a layout with a {@code merge} top level item, there is no
 * top level item that can use the {@code include} item as cookie.
 * <p/>
 * This class is used as a cookie for all items under the {@code merge} (while referencing the
 * original {@code include} cookie) to make it easy on the client to group all merged items
 * into a single outline item.
 *
 */
public final class MergeCookie {

    private final Object mCookie;

    public MergeCookie(Object cookie) {
        mCookie = cookie;

    }

    public Object getCookie() {
        return mCookie;
    }
}
