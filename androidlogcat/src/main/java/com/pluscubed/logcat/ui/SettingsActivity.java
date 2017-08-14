package com.pluscubed.logcat.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.Toast;

import com.pluscubed.logcat.R;
import com.pluscubed.logcat.data.LogLine;
import com.pluscubed.logcat.helper.PreferenceHelper;
import com.pluscubed.logcat.util.ArrayUtil;
import com.pluscubed.logcat.util.StringUtil;
import com.pluscubed.logcat.widget.MultipleChoicePreference;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);

        FragmentManager fm = getFragmentManager();
        Fragment f = fm.findFragmentById(R.id.content);
        if (f == null) {
            fm.beginTransaction()
                    .replace(R.id.content,
                            new SettingsFragment())
                    .commit();
        }
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.settings);
    }

    private void setResultAndFinish() {
        Intent data = new Intent();
        FragmentManager fm = getFragmentManager();
        SettingsFragment f = (SettingsFragment) fm.findFragmentById(R.id.content);
        data.putExtra("bufferChanged", f.getBufferChanged());
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            // set result and finish
            setResultAndFinish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                setResultAndFinish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragment implements OnPreferenceChangeListener {

        private static final int MAX_LOG_LINE_PERIOD = 1000;
        private static final int MIN_LOG_LINE_PERIOD = 1;
        private static final int MAX_DISPLAY_LIMIT = 100000;
        private static final int MIN_DISPLAY_LIMIT = 1000;

        private EditTextPreference logLinePeriodPreference, displayLimitPreference;
        private ListPreference textSizePreference, defaultLevelPreference;
        private MultipleChoicePreference bufferPreference;
        private SwitchPreference scrubberPreference;

        private boolean bufferChanged = false;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);

            setUpPreferences();
        }

        public boolean getBufferChanged() {
            return bufferChanged;
        }

        private void setUpPreferences() {

            displayLimitPreference = (EditTextPreference) findPreference(getString(R.string.pref_display_limit));

            int displayLimitValue = PreferenceHelper.getDisplayLimitPreference(getActivity());

            displayLimitPreference.setSummary(getString(R.string.pref_display_limit_summary,
                    displayLimitValue, getString(R.string.pref_display_limit_default)));

            displayLimitPreference.setOnPreferenceChangeListener(this);

            logLinePeriodPreference = (EditTextPreference) findPreference(getString(R.string.pref_log_line_period));

            int logLinePrefValue = PreferenceHelper.getLogLinePeriodPreference(getActivity());

            logLinePeriodPreference.setSummary(getString(R.string.pref_log_line_period_summary,
                    logLinePrefValue, getString(R.string.pref_log_line_period_default)));

            logLinePeriodPreference.setOnPreferenceChangeListener(this);

            textSizePreference = (ListPreference) findPreference(getString(R.string.pref_text_size));
            textSizePreference.setSummary(textSizePreference.getEntry());
            textSizePreference.setOnPreferenceChangeListener(this);

            defaultLevelPreference = (ListPreference) findPreference(getString(R.string.pref_default_log_level));
            defaultLevelPreference.setOnPreferenceChangeListener(this);
            setDefaultLevelPreferenceSummary(defaultLevelPreference.getEntry());


            bufferPreference = (MultipleChoicePreference) findPreference(getString(R.string.pref_buffer));
            bufferPreference.setOnPreferenceChangeListener(this);
            setBufferPreferenceSummary(bufferPreference.getValue());

            scrubberPreference = (SwitchPreference) getPreferenceScreen().findPreference("scrubber");
            scrubberPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    LogLine.isScrubberEnabled = (boolean) newValue;
                    return true;
                }
            });
        }

        private void setDefaultLevelPreferenceSummary(CharSequence entry) {
            defaultLevelPreference.setSummary(
                    getString(R.string.pref_default_log_level_summary, entry));

        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {

            if (preference.getKey().equals(getString(R.string.pref_display_limit))) {

                // display buffer preference; update summary

                String input = ((String) newValue).trim();

                try {

                    int value = Integer.parseInt(input);
                    if (value >= MIN_DISPLAY_LIMIT && value <= MAX_DISPLAY_LIMIT) {
                        PreferenceHelper.setDisplayLimitPreference(getActivity(), value);
                        displayLimitPreference.setSummary(getString(R.string.pref_display_limit_summary,
                                value, getString(R.string.pref_display_limit_default)));

                        // notify that a restart is required
                        Toast.makeText(getActivity(), R.string.toast_pref_changed_restart_required, Toast.LENGTH_LONG).show();

                        return true;
                    }

                } catch (NumberFormatException ignore) {
                }


                String invalidEntry = getString(R.string.toast_invalid_display_limit, MIN_DISPLAY_LIMIT, MAX_DISPLAY_LIMIT);
                Toast.makeText(getActivity(), invalidEntry, Toast.LENGTH_LONG).show();
                return false;

            } else if (preference.getKey().equals(getString(R.string.pref_log_line_period))) {

                // log line period preference; update summary

                String input = ((String) newValue).trim();

                try {

                    int value = Integer.parseInt(input);
                    if (value >= MIN_LOG_LINE_PERIOD && value <= MAX_LOG_LINE_PERIOD) {
                        PreferenceHelper.setLogLinePeriodPreference(getActivity(), value);
                        logLinePeriodPreference.setSummary(getString(R.string.pref_log_line_period_summary,
                                value, getString(R.string.pref_log_line_period_default)));
                        return true;
                    }

                } catch (NumberFormatException ignore) {
                }


                Toast.makeText(getActivity(), R.string.pref_log_line_period_error, Toast.LENGTH_LONG).show();
                return false;

            } else if (preference.getKey().equals(getString(R.string.pref_theme))) {
                // update summary
                /*int index = ArrayUtil.indexOf(mThemePreference.getEntryValues(), newValue.toString());
                CharSequence newEntry = mThemePreference.getEntries()[index];
                mThemePreference.setSummary(newEntry);*/

                return true;
            } else if (preference.getKey().equals(getString(R.string.pref_buffer))) {
                // buffers pref

                // check to make sure nothing was left unchecked
                if (TextUtils.isEmpty(newValue.toString())) {
                    Toast.makeText(getActivity(), R.string.pref_buffer_none_checked_error, Toast.LENGTH_SHORT).show();
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

                int index = ArrayUtil.indexOf(listPreference.getEntryValues(), newValue);
                CharSequence newEntry = listPreference.getEntries()[index];
                setDefaultLevelPreferenceSummary(newEntry);

                return true;

            } else { // text size pref

                // update the summary to reflect changes

                ListPreference listPreference = (ListPreference) preference;

                int index = ArrayUtil.indexOf(listPreference.getEntryValues(), newValue);
                CharSequence newEntry = listPreference.getEntries()[index];
                listPreference.setSummary(newEntry);

                return true;
            }

        }


        private void setBufferPreferenceSummary(String value) {

            String[] commaSeparated = StringUtil.split(StringUtil.nullToEmpty(value), MultipleChoicePreference.DELIMITER);

            List<CharSequence> checkedEntries = new ArrayList<CharSequence>();

            for (String entryValue : commaSeparated) {
                int idx = ArrayUtil.indexOf(bufferPreference.getEntryValues(), entryValue);
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
}
