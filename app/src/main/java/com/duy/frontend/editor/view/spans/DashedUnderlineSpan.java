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

package com.duy.frontend.editor.view.spans;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.text.Layout;
import android.text.style.LineBackgroundSpan;
import android.text.style.LineHeightSpan;
import android.widget.TextView;

/**
 * Created by Duy on 15-Apr-17.
 */

public class DashedUnderlineSpan implements LineBackgroundSpan, LineHeightSpan {
    private Paint paint;
    private TextView textView;
    private float offsetY;
    private float spacingExtra;

    public DashedUnderlineSpan(TextView textView, int color, float thickness, float dashPath,
                               float offsetY, float spacingExtra) {
        this.paint = new Paint();
        this.paint.setColor(color);
        this.paint.setStyle(Paint.Style.STROKE);
        this.paint.setPathEffect(new DashPathEffect(new float[]{dashPath, dashPath}, 0));
        this.paint.setStrokeWidth(thickness);
        this.textView = textView;
        this.offsetY = offsetY;
        this.spacingExtra = spacingExtra;
    }

    @Override
    public void chooseHeight(CharSequence text, int start, int end, int spanstartv, int v,
                             Paint.FontMetricsInt fm) {
        fm.ascent -= spacingExtra;
        fm.top -= spacingExtra;
        fm.descent += spacingExtra;
        fm.bottom += spacingExtra;
    }

    @Override
    public void drawBackground(Canvas canvas, Paint p, int left, int right, int top, int baseline,
                               int bottom, CharSequence text, int start, int end, int lnum) {
        int lineNum = textView.getLineCount();
        for (int i = 0; i < lineNum; i++) {
            Layout layout = textView.getLayout();
            canvas.drawLine(layout.getLineLeft(i), layout.getLineBottom(i) - spacingExtra + offsetY,
                    layout.getLineRight(i), layout.getLineBottom(i) - spacingExtra + offsetY,
                    this.paint);
        }
    }
}
