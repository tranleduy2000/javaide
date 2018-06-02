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
 * Rendering parameters for {@link Bridge#renderDrawable(DrawableParams)}
 *
 */
public class DrawableParams extends RenderParams {

    private final ResourceValue mDrawable;

    /**
    * Builds a param object with all the necessary parameters to render a drawable with
    * {@link Bridge#renderDrawable(DrawableParams)}
    *
    * @param drawable the {@link ResourceValue} identifying the drawable.
    * @param projectKey An Object identifying the project. This is used for the cache mechanism.
    * @param hardwareConfig the {@link HardwareConfig}.
    * @param renderResources a {@link RenderResources} object providing access to the resources.
    * @param layoutlibCallback The {@link LayoutlibCallback} object to get information from
    * the project.
    * @param minSdkVersion the minSdkVersion of the project
    * @param targetSdkVersion the targetSdkVersion of the project
    * @param log the object responsible for displaying warning/errors to the user.
    */
    public DrawableParams(
            ResourceValue drawable,
            Object projectKey,
            HardwareConfig hardwareConfig,
            RenderResources renderResources,
            LayoutlibCallback layoutlibCallback,
            int minSdkVersion, int targetSdkVersion,
            LayoutLog log) {
        super(projectKey, hardwareConfig,
                renderResources, layoutlibCallback, minSdkVersion, targetSdkVersion, log);
        mDrawable = drawable;
    }

    public DrawableParams(DrawableParams params) {
        super(params);
        mDrawable = params.mDrawable;
    }

    public ResourceValue getDrawable() {
        return mDrawable;
    }
}
