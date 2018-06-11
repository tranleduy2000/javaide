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
import android.text.style.LineBackgroundSpan;

public class CustomUnderlineSpan implements LineBackgroundSpan {

    int color;
    Paint p;
    int start, end;

    public CustomUnderlineSpan(int underlineColor, int underlineStart, int underlineEnd) {
        super();
        color = underlineColor;
        this.start = underlineStart;
        this.end = underlineEnd;
        p = new Paint();
        p.setColor(color);
        p.setStrokeWidth(3F);
        p.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    @Override
    public void drawBackground(Canvas c, Paint p, int left, int right, int top, int baseline,
                               int bottom, CharSequence text, int start, int end, int lnum) {

        if (this.end < start) return;
        if (this.start > end) return;

        int offsetX = 0;
        if (this.start > start) {
            offsetX = (int) p.measureText(text.subSequence(start, this.start).toString());
        }

        int length = (int) p.measureText(text.subSequence(Math.max(start, this.start), Math.min(end, this.end)).toString());
        c.drawLine(offsetX, baseline + 3F, length + offsetX, baseline + 3F, this.p);
    }
}