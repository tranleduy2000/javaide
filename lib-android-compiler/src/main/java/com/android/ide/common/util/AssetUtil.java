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

package com.android.ide.common.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * A set of utility classes for manipulating {@link Bitmap} objects and drawing them to
 * {@link Canvas} canvases.
 */
public class AssetUtil {
    /**
     * Scales the given rectangle by the given scale factor.
     *
     * @param rect        The rectangle to scale.
     * @param scaleFactor The factor to scale by.
     * @return The scaled rectangle.
     */
    public static Rect scaleRect(Rect rect, float scaleFactor) {
        return new Rect(
                Math.round(rect.left * scaleFactor),
                Math.round(rect.right * scaleFactor),
                Math.round(rect.width() * scaleFactor),
                Math.round(rect.height() * scaleFactor));
    }

    /**
     * An effect to apply in
     * {@link AssetUtil#drawEffects(java.awt.Canvas, java.awt.image.Bitmap, int, int, Effect[])}
     */
    public abstract static class Effect {
    }

    /**
     * An inner or outer shadow.
     */
    public static class ShadowEffect extends Effect {
        public double xOffset;
        public double yOffset;
        public double radius;
        public Color color;
        public double opacity;
        public boolean inner;

        public ShadowEffect(double xOffset, double yOffset, double radius, Color color,
                            double opacity, boolean inner) {
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.radius = radius;
            this.color = color;
            this.opacity = opacity;
            this.inner = inner;
        }
    }

    /**
     * A fill, defined by a paint.
     */
    public static class FillEffect extends Effect {
        public Paint paint;
        public double opacity;

        public FillEffect(Paint paint, double opacity) {
            this.paint = paint;
            this.opacity = opacity;
        }

        public FillEffect(Paint paint) {
            this.paint = paint;
            this.opacity = 1.0;
        }
    }
}
