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

/**
 * Log class for actions executed through {@link Bridge} and {@link RenderSession}.
 */
public class LayoutLog {
    /**
     * Prefix for resource warnings/errors. This is not meant to be used as-is by the Layout
     * Library, but is there to help test against a wider type of warning/error.
     * <p/>
     * {@code tag.startsWith(LayoutLog.TAG_RESOURCE_PREFIX} will test if the tag is any type
     * of resource warning/error
     */
    public static final String TAG_RESOURCES_PREFIX = "resources.";

    /**
     * Prefix for matrix warnings/errors. This is not meant to be used as-is by the Layout
     * Library, but is there to help test against a wider type of warning/error.
     * <p/>
     * {@code tag.startsWith(LayoutLog.TAG_MATRIX_PREFIX} will test if the tag is any type
     * of matrix warning/error
     */
    public static final String TAG_MATRIX_PREFIX = "matrix.";

    /**
     * Tag for unsupported feature that can have a big impact on the rendering. For instance, aild
     * access.
     */
    public static final String TAG_UNSUPPORTED = "unsupported";

    /**
     * Tag for error when something really unexpected happens.
     */
    public static final String TAG_BROKEN = "broken";

    /**
     * Tag for resource resolution failure.
     * In this case the warning/error data object will be a ResourceValue containing the type
     * and name of the resource that failed to resolve
     */
    public static final String TAG_RESOURCES_RESOLVE = TAG_RESOURCES_PREFIX + "resolve";

    /**
     * Tag for resource resolution failure, specifically for theme attributes.
     * In this case the warning/error data object will be a ResourceValue containing the type
     * and name of the resource that failed to resolve
     */
    public static final String TAG_RESOURCES_RESOLVE_THEME_ATTR = TAG_RESOURCES_RESOLVE + ".theme";

    /**
     * Tag for failure when reading the content of a resource file.
     */
    public static final String TAG_RESOURCES_READ = TAG_RESOURCES_PREFIX + "read";

    /**
     * Tag for wrong format in a resource value.
     */
    public static final String TAG_RESOURCES_FORMAT = TAG_RESOURCES_PREFIX + "format";

    /**
     * Fidelity Tag used when a non affine transformation matrix is used in a Java API.
     */
    public static final String TAG_MATRIX_AFFINE = TAG_MATRIX_PREFIX + "affine";

    /**
     * Tag used when a matrix cannot be inverted.
     */
    public static final String TAG_MATRIX_INVERSE = TAG_MATRIX_PREFIX + "inverse";

    /**
     * Fidelity Tag used when a mask filter type is used but is not supported.
     */
    public static final String TAG_MASKFILTER = "maskfilter";

    /**
     * Fidelity Tag used when a draw filter type is used but is not supported.
     */
    public static final String TAG_DRAWFILTER = "drawfilter";

    /**
     * Fidelity Tag used when a path effect type is used but is not supported.
     */
    public static final String TAG_PATHEFFECT = "patheffect";

    /**
     * Fidelity Tag used when a color filter type is used but is not supported.
     */
    public static final String TAG_COLORFILTER = "colorfilter";

    /**
     * Fidelity Tag used when a rasterize type is used but is not supported.
     */
    public static final String TAG_RASTERIZER = "rasterizer";

    /**
     * Fidelity Tag used when a shader type is used but is not supported.
     */
    public static final String TAG_SHADER = "shader";

    /**
     * Fidelity Tag used when an unrecognized format is found for strftime.
     */
    public static final String TAG_STRFTIME = "strftime";

    /**
     * Fidelity Tag used when a xfermode type is used but is not supported.
     */
    public static final String TAG_XFERMODE = "xfermode";

    /**
     * Tag for reporting missing assets, or problem loading them.
     */
    public static final String TAG_MISSING_ASSET = "missingasset";

    /**
     * Fidelity Tag used when something went wrong in rendering text.
     */
    public static final String TAG_TEXT_RENDERING = "textRendering";

    /**
     * Tag for reporting messages that should not be logged in the render error panel,
     * but silently logged to a more detailed log file.
     */
    public static final String TAG_INFO = "info";

    /**
     * Logs a warning.
     * @param tag a tag describing the type of the warning
     * @param message the message of the warning
     * @param data an optional data bundle that the client can use to improve the warning display.
     */
    public void warning(String tag, String message, Object data) {
    }

    /**
     * Logs a fidelity warning.
     *
     * This type of warning indicates that the render will not be
     * the same as the rendering on a device due to limitation of the Java rendering API.
     *
     * @param tag a tag describing the type of the warning
     * @param message the message of the warning
     * @param throwable an optional Throwable that triggered the warning
     * @param data an optional data bundle that the client can use to improve the warning display.
     */
    public void fidelityWarning(String tag, String message, Throwable throwable, Object data) {
    }

    /**
     * Logs an error.
     *
     * @param tag a tag describing the type of the error
     * @param message the message of the error
     * @param data an optional data bundle that the client can use to improve the error display.
     */
    public void error(String tag, String message, Object data) {
    }

    /**
     * Logs an error, and the {@link Throwable} that triggered it.
     *
     * @param tag a tag describing the type of the error
     * @param message the message of the error
     * @param throwable the Throwable that triggered the error
     * @param data an optional data bundle that the client can use to improve the error display.
     */
    public void error(String tag, String message, Throwable throwable, Object data) {
    }
}
