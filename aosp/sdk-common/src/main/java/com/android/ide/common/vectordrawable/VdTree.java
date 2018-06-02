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

package com.android.ide.common.vectordrawable;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Used to represent the whole VectorDrawable XML file's tree.
 */
class VdTree {
    private static Logger logger = Logger.getLogger(VdTree.class.getSimpleName());

    VdGroup mCurrentGroup = new VdGroup();
    ArrayList<VdElement> mChildren;

    float mBaseWidth = 1;
    float mBaseHeight = 1;
    float mPortWidth = 1;
    float mPortHeight = 1;
    float mRootAlpha = 1;

    /**
     * Ensure there is at least one animation for every path in group (linking
     * them by names) Build the "current" path based on the first group
     */
    void parseFinish() {
        mChildren = mCurrentGroup.getChildren();
    }

    void add(VdElement pathOrGroup) {
        mCurrentGroup.add(pathOrGroup);
    }

    float getBaseWidth() {
        return mBaseWidth;
    }

    float getBaseHeight() {
        return mBaseHeight;
    }

    private void drawInternal(Canvas g, int w, int h) {
//        float scaleX = w / mPortWidth;
//        float scaleY = h / mPortHeight;
//        float minScale = Math.min(scaleX, scaleY);
//
//        if (mChildren == null) {
//            logger.log(Level.FINE, "no pathes");
//            return;
//        }
//        ((Canvas) g).scale(scaleX, scaleY);
//
//        Rect bounds = null;
//        for (int i = 0; i < mChildren.size(); i++) {
//            // TODO: do things differently when it is a path or group!!
//            VdPath path = (VdPath) mChildren.get(i);
//            logger.log(Level.FINE, "mCurrentPaths[" + i + "]=" + path.getName() +
//                    Integer.toHexString(path.mFillColor));
//            if (mChildren.get(i) != null) {
//                Rect r = drawPath(path, g, w, h, minScale);
//                if (bounds == null) {
//                    bounds = r;
//                } else {
//                    bounds.add(r);
//                }
//            }
//        }
//        logger.log(Level.FINE, "Rect " + bounds);
//        logger.log(Level.FINE, "Port  " + mPortWidth + "," + mPortHeight);
//        double right = mPortWidth - bounds.getMaxX();
//        double bot = mPortHeight - bounds.getMaxY();
//        logger.log(Level.FINE, "x " + bounds.getMinX() + ", " + right);
//        logger.log(Level.FINE, "y " + bounds.getMinY() + ", " + bot);
    }

    private Rect drawPath(VdPath path, Canvas canvas, int w, int h, float scale) {

//        Path path2d = new Path();
//        Canvas g = (Canvas) canvas;
//        path.toPath(path2d);
//
//        // TODO: Use AffineTransform to apply group's transformation info.
//        double theta = Math.toRadians(path.mRotate);
//        g.rotate(theta, path.mRotateX, path.mRotateY);
//        if (path.mClip) {
//            logger.log(Level.FINE, "CLIP");
//
//            g.setColor(Color.RED);
//            g.fill(path2d);
//
//        }
//        if (path.mFillColor != 0) {
//            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//            g.setColor(new Color(path.mFillColor, true));
//            g.fill(path2d);
//        }
//        if (path.mStrokeColor != 0) {
//            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//            g.setStroke(new BasicStroke(path.mStrokeWidth));
//            g.setColor(new Color(path.mStrokeColor, true));
//            g.draw(path2d);
//        }
//
//        g.rotate(-theta, path.mRotateX, path.mRotateY);
//        return path2d.getBounds();

        return null;
    }

    /**
     * Draw the VdTree into an image.
     * If the root alpha is less than 1.0, then draw into a temporary image,
     * then draw into the result image applying alpha blending.
     */
    public void drawIntoImage(Bitmap image) {
//        Canvas gFinal = new Canvas();
//        int width = image.getWidth();
//        int height = image.getHeight();
//        gFinal.setColor(new Color(255, 255, 255, 0));
//        gFinal.fillRect(0, 0, width, height);
//
//        float rootAlpha = mRootAlpha;
//        if (rootAlpha < 1.0) {
//            Bitmap alphaImage = AssetUtil.newArgbBitmap(width, height);
//            Canvas gTemp = (Canvas) alphaImage.getCanvas();
//            drawInternal(gTemp, width, height);
//            gFinal.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, rootAlpha));
//            gFinal.drawImage(alphaImage, 0, 0, null);
//            gTemp.dispose();
//        } else {
//            drawInternal(gFinal, width, height);
//        }
//        gFinal.dispose();
    }
}
