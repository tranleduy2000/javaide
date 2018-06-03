package com.duy.ide.javaide.setting;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import com.duy.common.preferences.PreferencesNative;
import com.duy.ide.R;

public class CompilerSettingFragment extends PreferenceFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_compiler);
        PreferencesNative.bindPreferenceSummaryToValue(findPreference(getString(R.string.key_pref_source_compatibility)));
        PreferencesNative.bindPreferenceSummaryToValue(findPreference(getString(R.string.key_pref_target_compatibility)));

    }
}
