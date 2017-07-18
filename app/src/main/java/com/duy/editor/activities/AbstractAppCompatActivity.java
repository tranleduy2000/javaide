package com.duy.editor.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.duy.editor.R;
import com.duy.editor.setting.JavaPreferences;

/**
 * Created by duy on 18/07/2017.
 */

public class AbstractAppCompatActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    private JavaPreferences mPreferences;

    public JavaPreferences getPreferences() {
        return mPreferences;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = new JavaPreferences(this);
        mPreferences.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

    }

    @Override
    protected void onDestroy() {
        mPreferences.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    public void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }

}
