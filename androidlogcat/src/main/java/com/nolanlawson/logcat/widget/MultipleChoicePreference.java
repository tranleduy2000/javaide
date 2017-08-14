package com.nolanlawson.logcat.widget;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;
import android.util.AttributeSet;

import com.nolanlawson.logcat.util.StringUtil;

/**
 * Similar to a ListPreference, but uses a multi-choice list and saves the value as a comma-separated string.
 * @author nlawson
 *
 */
public class MultipleChoicePreference extends ListPreference {

	public static final String DELIMITER = ",";
	boolean[] checkedDialogEntryIndexes;
	
	public MultipleChoicePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MultipleChoicePreference(Context context) {
		super(context);
	}

	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		
		// convert comma-separated list to boolean array
		
		String value = getValue();
		Set<String> commaSeparated = new HashSet<String>(Arrays.asList(StringUtil.split(value, DELIMITER)));
		
		CharSequence[] entryValues = getEntryValues();
		final boolean[] checked = new boolean[entryValues.length];
		for (int i = 0; i < entryValues.length; i++) {
			checked[i] = commaSeparated.contains(entryValues[i]);
		}
		
		builder.setMultiChoiceItems(getEntries(), checked, new DialogInterface.OnMultiChoiceClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				checked[which] = isChecked;
			}
		});
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				checkedDialogEntryIndexes = checked;
				
				/*
                 * Clicking on an item simulates the positive button
                 * click, and dismisses the dialog.
                 */
				MultipleChoicePreference.this.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
				dialog.dismiss();
				
			}
		});
	}
	
	@Override
    protected void onDialogClosed(boolean positiveResult) {

        if (positiveResult && checkedDialogEntryIndexes != null) {
            String value = createValueAsString(checkedDialogEntryIndexes);
            if (callChangeListener(value)) {
                setValue(value);
            }
        }
    }
	
	private String createValueAsString(boolean[] checked) {
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < checked.length; i++) {
			if (checked[i]) {
				sb.append(getEntryValues()[i]).append(DELIMITER);
			}
		}
		return sb.length() == 0 ? "" : sb.substring(0, sb.length() - 1);
	}
}
