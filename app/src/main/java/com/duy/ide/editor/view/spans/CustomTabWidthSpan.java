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

package com.duy.ide.editor.view.spans;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.text.style.ReplacementSpan;

public class CustomTabWidthSpan extends ReplacementSpan {
    private int tabWidth = 3;

    public CustomTabWidthSpan(int tabWidth) {
        this.tabWidth = tabWidth;
    }

    @Override
    public int getSize(@NonNull Paint p1, CharSequence p2, int p3, int p4, Paint.FontMetricsInt p5) {
        return tabWidth;
    }

    @Override
    public void draw(@NonNull Canvas p1, CharSequence p2, int p3, int p4,
                     float p5, int p6, int p7, int p8, @NonNull Paint p9) {
    }
}