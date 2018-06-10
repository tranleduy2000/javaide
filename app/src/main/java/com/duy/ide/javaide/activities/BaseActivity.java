package com.duy.ide.javaide.activities;

import android.content.SharedPreferences;
import android.support.annotation.StyleRes;
import android.support.v7.widget.Toolbar;

import com.duy.ide.R;
import com.jecelyin.editor.v2.ThemeSupportActivity;

/**
 * Created by duy on 18/07/2017.
 */

public class BaseActivity extends ThemeSupportActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @StyleRes
    @Override
    public int getThemeId() {
        return R.style.AppThemeDark;
    }

    public void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

}
