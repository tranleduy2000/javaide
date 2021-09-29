/*
 * Copyright (C) 2018 Tran Le Duy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
