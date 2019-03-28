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

package com.android.ide.common.rendering.api;

import com.android.ide.common.rendering.api.Result.Status;

import java.io.File;
import java.util.EnumSet;
import java.util.Map;

import static com.android.ide.common.rendering.api.Result.Status.NOT_IMPLEMENTED;

public abstract class Bridge {

    public static final int API_CURRENT = 15;

    public abstract int getApiLevel();

    public int getRevision() {
        return 0;
    }

    @Deprecated
    public EnumSet<Capability> getCapabilities() {
        return EnumSet.noneOf(Capability.class);
    }

    public boolean supports(int feature) {
        return false;
    }

    public boolean init(Map<String, String> platformProperties,
            File fontLocation,
            Map<String, Map<String, Integer>> enumValueMap,
            LayoutLog log) {
        return false;
    }

    public boolean dispose() {
        return false;
    }

    public RenderSession createSession(SessionParams params) {
        return null;
    }

    public Result renderDrawable(DrawableParams params) {
        return Status.NOT_IMPLEMENTED.createResult();
    }

    public void clearCaches(Object projectKey) {

    }

    public Result getViewParent(Object viewObject) {
        return NOT_IMPLEMENTED.createResult();
    }

    public Result getViewIndex(Object viewObject) {
        return NOT_IMPLEMENTED.createResult();
    }

    public boolean isRtl(String locale) {
        return false;
    }

    @Deprecated
    public Result getViewBaseline(Object viewObject) {
        return NOT_IMPLEMENTED.createResult();
    }
}
