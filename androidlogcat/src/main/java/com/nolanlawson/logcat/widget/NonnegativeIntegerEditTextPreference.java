package com.nolanlawson.logcat.widget;

import android.content.Context;
import android.preference.EditTextPreference;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;

/**
 * EditTextPreference that only allows inputting integer numbers.
 * @author nlawson
 *
 */
public class NonnegativeIntegerEditTextPreference extends EditTextPreference {

	public NonnegativeIntegerEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setUpEditText();
	}

	public NonnegativeIntegerEditTextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setUpEditText();
	}

	public NonnegativeIntegerEditTextPreference(Context context) {
		super(context);
		setUpEditText();
	}	
	
	private void setUpEditText() {
		getEditText().setKeyListener(DigitsKeyListener.getInstance(false, false));
	}
}
