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
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.text.style.ReplacementSpan;

/**
 * Created by Duy on 31-May-17.
 */

public class ErrorSpan extends ReplacementSpan {
    private int color;

    public ErrorSpan(int color) {
        this.color = color;
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top,
                     int y, int bottom, @NonNull Paint paint) {
        RectF rect = new RectF(x, top, x + measureText(paint, text, start, end), bottom);

        Paint rectPaint = new Paint(paint);
        rectPaint.setAntiAlias(true);
        rectPaint.setStrokeWidth(2);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setColor(color);
        canvas.drawRect(rect, rectPaint);

        canvas.drawText(text, start, end, x, y, paint);
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return Math.round(measureText(paint, text, start, end));
    }

    private float measureText(Paint paint, CharSequence text, int start, int end) {
        return paint.measureText(text, start, end);
    }

}
