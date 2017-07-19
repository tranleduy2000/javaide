package com.duy.ide.editor.view.spans;

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
    public void drawBackground(Canvas c, Paint p, int left, int right, int top, int baseline, int bottom, CharSequence text, int start, int end, int lnum) {

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