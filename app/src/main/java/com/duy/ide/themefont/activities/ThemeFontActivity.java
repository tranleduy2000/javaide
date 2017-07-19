/*
 *  Copyright (c) 2017 Tran Le Duy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duy.ide.themefont.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.duy.ide.R;
import com.duy.ide.activities.AbstractAppCompatActivity;
import com.duy.ide.themefont.adapter.SectionPageAdapter;
import com.duy.ide.themefont.themes.ThemeFragment;
import com.google.firebase.analytics.FirebaseAnalytics;


/**
 * Created by Duy on 12-Mar-17.
 */

@SuppressWarnings("DefaultFileTemplate")
public class ThemeFontActivity extends AbstractAppCompatActivity
        implements ThemeFragment.OnThemeSelectListener {

    private Toolbar toolbar;
    private ViewPager viewPager;
    private SectionPageAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_font);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setupToolbar();

        FirebaseAnalytics.getInstance(this).logEvent("open_choose_font_theme", new Bundle());

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        adapter = new SectionPageAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
    }

    public void setupToolbar() {
        setSupportActionBar(toolbar);
        setTitle(R.string.theme);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onThemeSelect(String name) {

    }


}
