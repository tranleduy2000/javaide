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


import static com.android.ide.common.rendering.api.Result.Status.NOT_IMPLEMENTED;

import com.android.ide.common.rendering.api.Result.Status;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.EnumSet;
import java.util.Map;

/**
 * Entry point of the Layout Library. Extensions of this class provide a method to compute
 * and render a layout.
 */
@SuppressWarnings({"MethodMayBeStatic", "UnusedDeclaration"})
public abstract class Bridge {

    public static final int API_CURRENT = 15;

    /**
     * Returns the API level of the layout library.
     * <p/>
     * While no methods will ever be removed, some may become deprecated, and some new ones
     * will appear.
     * <p/>All Layout libraries based on {@link Bridge} return at minimum an API level of 5.
     */
    public abstract int getApiLevel();

    /**
     * Returns the revision of the library inside a given (layoutlib) API level.
     * The true revision number of the library is {@link #getApiLevel()}.{@link #getRevision()}
     */
    @SuppressWarnings("JavaDoc")  // javadoc pointing to itself.
    public int getRevision() {
        return 0;
    }

    /**
     * Returns an {@link EnumSet} of the supported {@link Capability}.
     *
     * @return an {@link EnumSet} with the supported capabilities.
     *
     * @deprecated use {@link #supports(int)}
     */
    @Deprecated
    public EnumSet<Capability> getCapabilities() {
        return EnumSet.noneOf(Capability.class);
    }

    /**
     * Returns true if the layout library supports the given feature.
     *
     * @see com.android.ide.common.rendering.api.Features
     */
    public boolean supports(int feature) {
        return false;
    }

    /**
     * Initializes the Bridge object.
     *
     * @param platformProperties The build properties for the platform.
     * @param fontLocation the location of the fonts.
     * @param enumValueMap map attrName => { map enumFlagName => Integer value }. This is typically
     *          read from attrs.xml in the SDK target.
     * @param log a {@link LayoutLog} object. Can be null.
     * @return true if success.
     */
    public boolean init(Map<String, String> platformProperties,
            File fontLocation,
            Map<String, Map<String, Integer>> enumValueMap,
            LayoutLog log) {
        return false;
    }

    /**
     * Prepares the layoutlib to unloaded.
     */
    public boolean dispose() {
        return false;
    }

    /**
     * Starts a layout session by inflating and rendering it. The method returns a
     * {@link RenderSession} on which further actions can be taken.
     *
     * @return a new {@link RenderSession} object that contains the result of the scene creation and
     * first rendering.
     */
    public RenderSession createSession(SessionParams params) {
        return null;
    }

    /**
     * Renders a Drawable. If the rendering is successful, the result image is accessible through
     * {@link Result#getData()}. It is of type {@link BufferedImage}
     * @param params the rendering parameters.
     * @return the result of the action.
     */
    public Result renderDrawable(DrawableParams params) {
        return Status.NOT_IMPLEMENTED.createResult();
    }

    /**
     * Clears the resource cache for a specific project.
     * <p/>This cache contains bitmaps and nine patches that are loaded from the disk and reused
     * until this method is called.
     * <p/>The cache is not configuration dependent and should only be cleared when a
     * resource changes (at this time only bitmaps and 9 patches go into the cache).
     * <p/>
     * The project key provided must be similar to the one passed in {@link RenderParams}.
     *
     * @param projectKey the key for the project.
     */
    public void clearCaches(Object projectKey) {

    }

    /**
     * Utility method returning the parent of a given view object.
     *
     * @param viewObject the object for which to return the parent.
     *
     * @return a {@link Result} indicating the status of the action, and if success, the parent
     *      object in {@link Result#getData()}
     */
    public Result getViewParent(Object viewObject) {
        return NOT_IMPLEMENTED.createResult();
    }

    /**
     * Utility method returning the index of a given view in its parent.
     * @param viewObject the object for which to return the index.
     *
     * @return a {@link Result} indicating the status of the action, and if success, the index in
     *      the parent in {@link Result#getData()}
     */
    public Result getViewIndex(Object viewObject) {
        return NOT_IMPLEMENTED.createResult();
    }

    /**
     * Returns true if the character orientation of the locale is right to left.
     * @param locale The locale formatted as language-region
     * @return true if the locale is right to left.
     */
    public boolean isRtl(String locale) {
        return false;
    }

    /**
     * Utility method returning the baseline value for a given view object. This basically returns
     * View.getBaseline().
     *
     * @param viewObject the object for which to return the index.
     *
     * @return the baseline value or -1 if not applicable to the view object or if this layout
     *     library does not implement this method.
     *
     * @deprecated use the extended ViewInfo.
     */
    @Deprecated
    public Result getViewBaseline(Object viewObject) {
        return NOT_IMPLEMENTED.createResult();
    }
}
