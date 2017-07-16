/*
 *  Copyright (c) 2017 Tran Le Duy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fsck.splitview;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;

public class SplitView extends LinearLayout implements OnTouchListener {

    final static private int MAXIMIZED_VIEW_TOLERANCE_DIP = 30;
    final static private int TAP_DRIFT_TOLERANCE = 3;
    final static private int SINGLE_TAP_MAX_TIME = 175;
    private int mHandleId;
    private View mHandle;

    private int mPrimaryContentId;
    private View mPrimaryContent;

    private int mSecondaryContentId;
    private View mSecondaryContent;

    private int mLastPrimaryContentSize;
    private boolean mDragging;
    private long mDraggingStarted;
    private float mDragStartX;
    private float mDragStartY;
    private float mPointerOffset;
    private float mMinSizePrimaryContent;

    public SplitView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public SplitView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        TypedArray viewAttrs = context.obtainStyledAttributes(attrs, R.styleable.SplitView);

        RuntimeException e = null;
        mHandleId = viewAttrs.getResourceId(R.styleable.SplitView_handle, 0);
        if (mHandleId == 0) {
            e = new IllegalArgumentException(viewAttrs.getPositionDescription() +
                    ": The required attribute handle must refer to a valid child view.");
        }

        mPrimaryContentId = viewAttrs.getResourceId(R.styleable.SplitView_topContent, 0);
        if (mPrimaryContentId == 0) {
            e = new IllegalArgumentException(viewAttrs.getPositionDescription() +
                    ": The required attribute primaryContent must refer to a valid child view.");
        }


        mSecondaryContentId = viewAttrs.getResourceId(R.styleable.SplitView_bottomContent, 0);
        if (mSecondaryContentId == 0) {
            e = new IllegalArgumentException(viewAttrs.getPositionDescription() +
                    ": The required attribute secondaryContent must refer to a valid child view.");
        }

        mMinSizePrimaryContent = viewAttrs.getDimension(R.styleable.SplitView_minSize, 1);
        viewAttrs.recycle();

        if (e != null) {
            throw e;
        }
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        mHandle = findViewById(mHandleId);
        if (mHandle == null) {
            String name = getResources().getResourceEntryName(mHandleId);
            throw new RuntimeException("Your Panel must have a child View whose id attribute is 'R.id." + name + "'");

        }
        mPrimaryContent = findViewById(mPrimaryContentId);
        if (mPrimaryContent == null) {
            String name = getResources().getResourceEntryName(mPrimaryContentId);
            throw new RuntimeException("Your Panel must have a child View whose id attribute is 'R.id." + name + "'");

        }

        mLastPrimaryContentSize = getPrimaryContentSize();

        mSecondaryContent = findViewById(mSecondaryContentId);
        if (mSecondaryContent == null) {
            String name = getResources().getResourceEntryName(mSecondaryContentId);
            throw new RuntimeException("Your Panel must have a child View whose id attribute is 'R.id." + name + "'");

        }

        mHandle.setOnTouchListener(this);

    }

    @Override
    public boolean onTouch(View view, MotionEvent me) {
        // Only capture drag events if we start
        if (view != mHandle) {
            return false;
        }
        //Log.v("foo", "at "+SystemClock.elapsedRealtime()+" got touch event " + me);
        if (me.getAction() == MotionEvent.ACTION_DOWN) {
            mDragging = true;
            mDraggingStarted = SystemClock.elapsedRealtime();
            mDragStartX = me.getX();
            mDragStartY = me.getY();
            if (getOrientation() == VERTICAL) {
                mPointerOffset = me.getRawY() - getPrimaryContentSize();
            } else {
                mPointerOffset = me.getRawX() - getPrimaryContentSize();
            }
            return true;
        } else if (me.getAction() == MotionEvent.ACTION_UP) {
            mDragging = false;
            /*if (mDragStartX < (me.getX() + TAP_DRIFT_TOLERANCE) &&
                            mDragStartX > (me.getX() - TAP_DRIFT_TOLERANCE) &&
                            mDragStartY < (me.getY() + TAP_DRIFT_TOLERANCE) &&
                            mDragStartY > (me.getY() - TAP_DRIFT_TOLERANCE) &&
                            ((SystemClock.elapsedRealtime() - mDraggingStarted) < SINGLE_TAP_MAX_TIME)) {
                if (isPrimaryContentMaximized() || isSecondaryContentMaximized()) {
                    setPrimaryContentSize(mLastPrimaryContentSize);
                } else {
                    maximizeSecondaryContent();
                }
            }*/
            return true;
        } else if (me.getAction() == MotionEvent.ACTION_MOVE) {
            if (getOrientation() == VERTICAL) {
                setPrimaryContentHeight((int) (me.getRawY() - mPointerOffset));
            } else {
                setPrimaryContentWidth((int) (me.getRawX() - mPointerOffset));
            }
        }
        return true;
    }


    public View getHandle() {
        return mHandle;
    }

    public int getPrimaryContentSize() {
        if (getOrientation() == VERTICAL) {
            return mPrimaryContent.getMeasuredHeight();
        } else {
            return mPrimaryContent.getMeasuredWidth();
        }

    }

    public boolean setPrimaryContentSize(int newSize) {
        if (getOrientation() == VERTICAL) {
            return setPrimaryContentHeight(newSize);
        } else {
            return setPrimaryContentWidth(newSize);
        }
    }


    private boolean setPrimaryContentHeight(int newHeight) {
        // the new primary content height should not be less than 0 to make the
        // handler always visible
        newHeight = (int) Math.max(mMinSizePrimaryContent, newHeight);
        // the new primary content height should not be more than the SplitView
        // height minus handler height to make the handler always visible 
        newHeight = Math.min(newHeight, getMeasuredHeight() - mHandle.getMeasuredHeight());
        LayoutParams params = (LayoutParams) mPrimaryContent
                .getLayoutParams();
        if (mSecondaryContent.getMeasuredHeight() < 1 && newHeight > params.height) {
            return false;
        }
        if (newHeight >= 0) {
            params.height = newHeight;
            // set the primary content parameter to do not stretch anymore and
            // use the height specified in the layout params
            params.weight = 0;
        }
        unMinimizeSecondaryContent();
        mPrimaryContent.setLayoutParams(params);
        return true;

    }

    private boolean setPrimaryContentWidth(int newWidth) {
        // the new primary content width should not be less than 0 to make the
        // handler always visible
        newWidth = (int) Math.max(mMinSizePrimaryContent, newWidth);
        // the new primary content width should not be more than the SplitView
        // width minus handler width to make the handler always visible 
        newWidth = Math.min(newWidth, getMeasuredWidth() - mHandle.getMeasuredWidth());
        LayoutParams params = (LayoutParams) mPrimaryContent.getLayoutParams();


        if (mSecondaryContent.getMeasuredWidth() < 1 && newWidth > params.width) {
            return false;
        }
        if (newWidth >= 0) {
            params.width = newWidth;
            // set the primary content parameter to do not stretch anymore and
            // use the width specified in the layout params
            params.weight = 0;
        }
        unMinimizeSecondaryContent();
        mPrimaryContent.setLayoutParams(params);
        return true;
    }

    public boolean isPrimaryContentMaximized() {
        if ((getOrientation() == VERTICAL && (mSecondaryContent.getMeasuredHeight() < MAXIMIZED_VIEW_TOLERANCE_DIP)) ||
                (getOrientation() == HORIZONTAL && (mSecondaryContent.getMeasuredWidth() < MAXIMIZED_VIEW_TOLERANCE_DIP))) {
            return true;
        } else {
            return false;
        }

    }

    public boolean isSecondaryContentMaximized() {
        if ((getOrientation() == VERTICAL && (mPrimaryContent.getMeasuredHeight() < MAXIMIZED_VIEW_TOLERANCE_DIP)) ||
                (getOrientation() == HORIZONTAL && (mPrimaryContent.getMeasuredWidth() < MAXIMIZED_VIEW_TOLERANCE_DIP))) {
            return true;
        } else {
            return false;
        }
    }

    public void maximizePrimaryContent() {
        maximizeContentPane(mPrimaryContent, mSecondaryContent);
    }

    public void maximizeSecondaryContent() {
        maximizeContentPane(mSecondaryContent, mPrimaryContent);
    }


    private void maximizeContentPane(View toMaximize, View toUnMaximize) {
        mLastPrimaryContentSize = getPrimaryContentSize();

        LayoutParams params = (LayoutParams) toUnMaximize
                .getLayoutParams();
        LayoutParams secondaryParams = (LayoutParams) toMaximize
                .getLayoutParams();
        // set the primary content parameter to do not stretch anymore and use
        // the height/width specified in the layout params
        params.weight = 0;
        // set the secondary content parameter to use all the available space
        secondaryParams.weight = 1;
        if (getOrientation() == VERTICAL) {
            params.height = 1;
        } else {
            params.width = 1;
        }
        toUnMaximize.setLayoutParams(params);
        toMaximize.setLayoutParams(secondaryParams);


    }

    private void unMinimizeSecondaryContent() {
        LayoutParams secondaryParams = (LayoutParams) mSecondaryContent.getLayoutParams();
        // set the secondary content parameter to use all the available space
        secondaryParams.weight = 1;
        mSecondaryContent.setLayoutParams(secondaryParams);

    }

};
