package com.nolanlawson.logcat.widget;

import android.content.Context;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;

/**
 * DialogPreference that shows the little "down" arrow but doesn't actually pop up a dialog window.  Useful for when you
 * want to just attach a custom onPreferenceClick action to a Preference.
 * @author nlawson
 *
 */
public class NoPopupDialogPreference extends DialogPreference {

	public NoPopupDialogPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public NoPopupDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void showDialog(Bundle state) {
		// do nothing
	}

	
	
}
