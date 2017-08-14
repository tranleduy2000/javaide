package com.nolanlawson.logcat;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.Toast;

import com.nolanlawson.logcat.helper.PackageHelper;
import com.nolanlawson.logcat.helper.PreferenceHelper;
import com.nolanlawson.logcat.util.ArrayUtil;
import com.nolanlawson.logcat.util.StringUtil;
import com.nolanlawson.logcat.widget.MockDisabledListPreference;
import com.nolanlawson.logcat.widget.MultipleChoicePreference;

public class SettingsActivity extends PreferenceActivity implements OnPreferenceChangeListener {
	
	private static final int MAX_LOG_LINE_PERIOD = 1000;
	private static final int MIN_LOG_LINE_PERIOD = 1;
	private static final int MAX_DISPLAY_LIMIT = 100000;
	private static final int MIN_DISPLAY_LIMIT = 1000;
	
	private EditTextPreference logLinePeriodPreference, displayLimitPreference;
	private ListPreference textSizePreference, defaultLevelPreference;
	private MultipleChoicePreference bufferPreference;
	private MockDisabledListPreference themePreference;
	private Preference aboutPreference;
	
	private boolean bufferChanged = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.settings);
		
		setUpPreferences();
	}
	
	private void setUpPreferences() {
		
		displayLimitPreference = (EditTextPreference) findPreference(getString(R.string.pref_display_limit));
		
		int displayLimitValue = PreferenceHelper.getDisplayLimitPreference(this);
		
		displayLimitPreference.setSummary(String.format(getString(R.string.pref_display_limit_summary).toString(),
				displayLimitValue, getString(R.string.pref_display_limit_default)));
		
		displayLimitPreference.setOnPreferenceChangeListener(this);
		
		logLinePeriodPreference = (EditTextPreference) findPreference(getString(R.string.pref_log_line_period));
		
		int logLinePrefValue = PreferenceHelper.getLogLinePeriodPreference(this);
		
		logLinePeriodPreference.setSummary(String.format(getString(R.string.pref_log_line_period_summary).toString(),
				logLinePrefValue, getString(R.string.pref_log_line_period_default)));
		
		logLinePeriodPreference.setOnPreferenceChangeListener(this);
		
		textSizePreference = (ListPreference) findPreference(getString(R.string.pref_text_size));
		textSizePreference.setSummary(textSizePreference.getEntry());
		textSizePreference.setOnPreferenceChangeListener(this);
		
		defaultLevelPreference = (ListPreference) findPreference(getString(R.string.pref_default_log_level));
		defaultLevelPreference.setOnPreferenceChangeListener(this);
		setDefaultLevelPreferenceSummary(defaultLevelPreference.getEntry());
		
		themePreference = (MockDisabledListPreference) findPreference(getString(R.string.pref_theme));
		themePreference.setOnPreferenceChangeListener(this);
		
		bufferPreference = (MultipleChoicePreference) findPreference(getString(R.string.pref_buffer));
		bufferPreference.setOnPreferenceChangeListener(this);
		setBufferPreferenceSummary(bufferPreference.getValue());
		
		boolean donateInstalled = PackageHelper.isCatlogDonateInstalled(this);
		
		String themeSummary = donateInstalled 
				? PreferenceHelper.getColorScheme(this).getDisplayableName(this)
				: getString(R.string.pref_theme_summary_free);
		
		themePreference.setSummary(themeSummary);
		themePreference.setEnabledAppearance(donateInstalled);
		if (!donateInstalled) {
			themePreference.overrideOnClick(new OnPreferenceClickListener() {
				
				@Override
				public boolean onPreferenceClick(Preference preference) {
					openDonateVersionInMarket();
					return true;
				}
			});
		}
		
		aboutPreference = findPreference(getString(R.string.pref_about));
		aboutPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				// launch about activity
				Intent intent = new Intent(SettingsActivity.this, AboutActivity.class);
				startActivity(intent);
				return true;
			}
		});
		aboutPreference.setSummary(String.format(getString(R.string.version), PackageHelper.getVersionName(this)));
	}
	
	private void setDefaultLevelPreferenceSummary(CharSequence entry) {
		defaultLevelPreference.setSummary(
				String.format(getString(R.string.pref_default_log_level_summary), entry));
		
	}

	private void openDonateVersionInMarket() {
		
		Intent intent = new Intent(Intent.ACTION_VIEW, 
				Uri.parse("market://details?id=com.nolanlawson.logcat.donate"));
		startActivity(intent);
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		
		if (preference.getKey().equals(getString(R.string.pref_display_limit))) {

			// display buffer preference; update summary
			
			String input = ((String)newValue).trim();

			try {
				
				int value = Integer.parseInt(input);
				if (value >= MIN_DISPLAY_LIMIT && value <= MAX_DISPLAY_LIMIT) {
					PreferenceHelper.setDisplayLimitPreference(this, value);
					displayLimitPreference.setSummary(String.format(getString(R.string.pref_display_limit_summary).toString(),
							value, getString(R.string.pref_display_limit_default)));
					
					// notify that a restart is required
					Toast.makeText(this, R.string.toast_pref_changed_restart_required, Toast.LENGTH_LONG).show();
					
					return true;
				}
				
			} catch (NumberFormatException ignore) { }
			

			String invalidEntry = String.format(getString(R.string.toast_invalid_display_limit), MIN_DISPLAY_LIMIT, MAX_DISPLAY_LIMIT);
			Toast.makeText(this, invalidEntry, Toast.LENGTH_LONG).show();
			return false;			
			
		} else if (preference.getKey().equals(getString(R.string.pref_log_line_period))) {
			
			// log line period preference; update summary
			
			String input = ((String)newValue).trim();

			try {
				
				int value = Integer.parseInt(input);
				if (value >= MIN_LOG_LINE_PERIOD && value <= MAX_LOG_LINE_PERIOD) {
					PreferenceHelper.setLogLinePeriodPreference(this, value);
					logLinePeriodPreference.setSummary(String.format(getString(R.string.pref_log_line_period_summary).toString(),
							value, getString(R.string.pref_log_line_period_default)));
					return true;
				}
				
			} catch (NumberFormatException ignore) { }
			

			Toast.makeText(this, R.string.pref_log_line_period_error, Toast.LENGTH_LONG).show();
			return false;
			
		} else if (preference.getKey().equals(getString(R.string.pref_theme))) {
			// update summary
			int index = ArrayUtil.indexOf(themePreference.getEntryValues(),newValue.toString());
			CharSequence newEntry = themePreference.getEntries()[index];
			themePreference.setSummary(newEntry);
			
			return true;		
		} else if (preference.getKey().equals(getString(R.string.pref_buffer))) {
			// buffers pref
			
			// check to make sure nothing was left unchecked
			if (TextUtils.isEmpty(newValue.toString())) {
				Toast.makeText(this, R.string.pref_buffer_none_checked_error, Toast.LENGTH_SHORT).show();
				return false;
			}
			
			// notify the LogcatActivity that the buffer has changed
			if (!newValue.toString().equals(bufferPreference.getValue())) {
				bufferChanged = true;
			}
			
			setBufferPreferenceSummary(newValue.toString());
			return true;
		} else if (preference.getKey().equals(getString(R.string.pref_default_log_level))) {
			// default log level preference
			
			// update the summary to reflect changes
			
			ListPreference listPreference = (ListPreference) preference;
			
			int index = ArrayUtil.indexOf(listPreference.getEntryValues(),newValue);
			CharSequence newEntry = listPreference.getEntries()[index];
			setDefaultLevelPreferenceSummary(newEntry);
			
			return true;
			
		} else { // text size pref
			
			// update the summary to reflect changes
			
			ListPreference listPreference = (ListPreference) preference;
			
			int index = ArrayUtil.indexOf(listPreference.getEntryValues(),newValue);
			CharSequence newEntry = listPreference.getEntries()[index];
			listPreference.setSummary(newEntry);
			
			return true;
		}
		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
		
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 ) {
	    	
	    	// set result and finish
	    	Intent data = new Intent();
	    	data.putExtra("bufferChanged", bufferChanged);
	    	setResult(RESULT_OK, data);
	    	finish();
	    	return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	private void setBufferPreferenceSummary(String value) {
		
		String[] commaSeparated = StringUtil.split(StringUtil.nullToEmpty(value), MultipleChoicePreference.DELIMITER);
		
		List<CharSequence> checkedEntries = new ArrayList<CharSequence>();
		
		for (String entryValue : commaSeparated) {
			int idx = ArrayUtil.indexOf(bufferPreference.getEntryValues(),entryValue);
			checkedEntries.add(bufferPreference.getEntries()[idx]);
		}
		
		String summary = TextUtils.join(getString(R.string.delimiter), checkedEntries);
		
		// add the word "simultaneous" to make it clearer what's going on with 2+ buffers
		if (checkedEntries.size() > 1) {
			summary += getString(R.string.simultaneous);
		}
		bufferPreference.setSummary(summary);
	}
}
