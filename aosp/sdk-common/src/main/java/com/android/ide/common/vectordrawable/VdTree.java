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

import android.graphics.Bitmap;//TODO fix it:
import android.graphics.Canvas;//TODO fix it:
import android.graphics.Rect;//TODO fix it:

import java.util.ArrayList;
import java.util.logging.Logger;

class VdTree {
    private static Logger logger = Logger.getLogger(VdTree.class.getSimpleName());

    VdGroup mCurrentGroup = new VdGroup();
    ArrayList<VdElement> mChildren;

    float mBaseWidth = 1;
    float mBaseHeight = 1;
    float mPortWidth = 1;
    float mPortHeight = 1;
    float mRootAlpha = 1;

    void parseFinish() {
        mChildren = mCurrentGroup.getChildren();
    }

    void add(VdElement pathOrGroup) {
        mCurrentGroup.add(pathOrGroup);
    }

    float getBaseWidth(){
        return mBaseWidth;
    }

    float getBaseHeight(){
        return mBaseHeight;
    }

    private void drawInternal(Canvas g, int w, int h) {

    }

    private Rect drawPath(VdPath path, Canvas canvas, int w, int h, float scale) {
        return null;
    }

    public void drawIntoImage(Bitmap image) {

    }
}
