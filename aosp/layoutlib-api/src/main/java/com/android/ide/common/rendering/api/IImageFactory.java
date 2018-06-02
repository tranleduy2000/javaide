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


import android.graphics.Bitmap;


/**
 * Image Factory Interface.
 *
 * An Image factory's task is to create the {@link Bitmap} into which the scene will be
 * rendered. The goal is to let the layoutlib caller create an image that's optimized for its use
 * case.
 *
 * If no factory is passed in {@link RenderParams#setImageFactory(IImageFactory)}, then a default
 * {@link Bitmap} of type {@link Bitmap#TYPE_INT_ARGB} is created.
 *
 */
public interface IImageFactory {

    /**
     * Creates a buffered image with the given size
     * @param width the width of the image
     * @param height the height of the image
     * @return a new (or reused) BufferedImage of the given size.
     */
    Bitmap getImage(int width, int height);
}
