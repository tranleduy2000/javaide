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

package com.duy.ide.java.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class LockableScrollView extends ScrollView {
    public static final String TAG = LockableScrollView.class.getSimpleName();
    private int lastY;
    private boolean scrollable = true;
    private ScrollListener scrollListener;

    public LockableScrollView(Context context) {
        super(context);
    }

    public LockableScrollView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public void setScrollingEnabled(boolean enabled) {
        scrollable = enabled;
    }

    public boolean isScrollable() {
        return scrollable;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return scrollable && super.onInterceptTouchEvent(ev);
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return !(ev.getAction() == MotionEvent.ACTION_DOWN && !scrollable) && super.onTouchEvent(ev);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        if (scrollListener == null || !scrollable) return;

        if (Math.abs(lastY - t) > 100) {
            lastY = t;
            if (scrollListener != null) scrollListener.onScroll(l, t);
        }
    }

    public void setScrollListener(ScrollListener scrollListener) {
        this.scrollListener = scrollListener;
    }

    public interface ScrollListener {
        void onScroll(int x, int y);
    }
}

