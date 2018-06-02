/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.ide.common.rendering.legacy;

import com.android.ide.common.rendering.api.ILayoutPullParser;
import com.android.layoutlib.api.IXmlPullParser;

/**
 * Intermediary interface extending both old and new project pull parsers from the layout lib API.
 *
 * Clients should use this instead of {@link ILayoutPullParser} or {@link IXmlPullParser}.
 *
 */
@SuppressWarnings("deprecation")
public interface ILegacyPullParser extends ILayoutPullParser, IXmlPullParser {

}
