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
