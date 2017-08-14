package com.nolanlawson.logcat.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

import com.nolanlawson.logcat.util.UtilLogger;

public class ExceptionCatchingListView extends ListView {

	private static UtilLogger log = new UtilLogger(ExceptionCatchingListView.class);
	
	public ExceptionCatchingListView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public ExceptionCatchingListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ExceptionCatchingListView(Context context) {
		super(context);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		try {
			return super.onTouchEvent(ev);
		} catch (Exception e) {
			log.d(e, "");
			return false;
		}
	}
}
