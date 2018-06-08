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

package com.duy.ide.java.editor.code.view;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by Duy on 15-Mar-17.
 */

public class EditorView extends HighlightEditor {
    private static final String TAG = EditorView.class.getSimpleName();

    public EditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditorView(Context context) {
        super(context);

    }
    public EditorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
