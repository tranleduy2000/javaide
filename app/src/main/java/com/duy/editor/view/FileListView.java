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

package com.duy.editor.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.ListViewCompat;
import android.util.AttributeSet;

import com.duy.editor.file.FragmentFileManager;

/**
 * This view will be inside {@link FragmentFileManager}
 * Created by Duy on 11-Feb-17.
 */
public class FileListView extends ListViewCompat {
    private static final String TAG = FileListView.class.getSimpleName();
    int lastEvent = -1;
    boolean isLastEventIntercepted = false;
    private float xDistance, yDistance, lastX, lastY;

    public FileListView(Context context) {
        super(context);
        setup(context);

    }

    public FileListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setup(context);

    }

    public FileListView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setup(context);
    }

    /**
     * set up recycle view, include layout manager, adapter , listener
     *
     * @param context - android mContext
     */
    private void setup(Context context) {

    }

}
