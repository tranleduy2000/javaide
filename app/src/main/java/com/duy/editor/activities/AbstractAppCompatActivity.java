package com.duy.editor.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.duy.editor.setting.JavaPreferences;

/**
 * Created by duy on 18/07/2017.
 */

public class AbstractAppCompatActivity extends AppCompatActivity {
    private JavaPreferences mPreferences;

    public JavaPreferences getPreferences() {
        return mPreferences;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = new JavaPreferences(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
