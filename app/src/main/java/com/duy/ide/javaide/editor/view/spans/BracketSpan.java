/*
 * Copyright (C) 2018 Tran Le Duy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.duy.ide.javaide.editor.view.spans;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.style.ReplacementSpan;

/**
 * Created by Duy on 21-Jul-17.
 */

public class BracketSpan extends ReplacementSpan {
    private int backgroundColor;
    private int textColor;
    private static final String TAG = "BracketSpan";
    private Paint mBackgroundPaint = new Paint();
    public BracketSpan(int backgroundColor, int textColor) {
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        mBackgroundPaint.setColor(backgroundColor);
        mBackgroundPaint.setStyle(Paint.Style.STROKE);
        mBackgroundPaint.setStrokeWidth(1f);
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text,
                       @IntRange(from = 0) int start, @IntRange(from = 0) int end,
                       @Nullable Paint.FontMetricsInt fm) {
        return Math.round(paint.measureText(text, start, end));
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text,
                     @IntRange(from = 0) int start, @IntRange(from = 0) int end,
                     float x, int top, int y, int bottom, @NonNull Paint paint) {

        RectF rect = new RectF(x, top, x + measureText(paint, text, start, end), bottom);
        canvas.drawRect(rect, mBackgroundPaint);
        canvas.drawText(text, start, end, x, y, paint);
    }

    private float measureText(Paint paint, CharSequence text, int start, int end) {
        return paint.measureText(text, start, end);
    }
}
