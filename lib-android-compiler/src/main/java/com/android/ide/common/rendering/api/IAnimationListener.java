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


import java.awt.image.BufferedImage;

public interface IAnimationListener {
    /**
     * Called when a new animation frame is available for display.
     *
     * <p>The {@link RenderSession} object is provided as a convenience. It should be queried
     * for the image through {@link RenderSession#getImage()}.
     *
     * <p>If no {@link IImageFactory} is used, then each new animation frame will be rendered
     * in its own new {@link BufferedImage} object. However if an image factory is used, and it
     * always re-use the same object, then the image is only guaranteed to be valid during
     * this method call. As soon as this method return the image content will be overridden
     * with new drawing.
     *
     */
    void onNewFrame(RenderSession scene);

    /**
     * Called when the animation is done playing.
     */
    void done(Result result);

    /**
     * Return true to cancel the animation.
     */
    boolean isCanceled();

}
