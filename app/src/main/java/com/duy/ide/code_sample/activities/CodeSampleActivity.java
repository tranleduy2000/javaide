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

package com.duy.ide.code_sample.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.commonsware.cwac.pager.PageDescriptor;
import com.commonsware.cwac.pager.SimplePageDescriptor;
import com.duy.ide.R;
import com.duy.ide.activities.AbstractAppCompatActivity;
import com.duy.ide.run.ExecuteActivity;
import com.duy.ide.code.CompileManager;
import com.duy.ide.code_sample.adapters.CodePagerAdapter;
import com.duy.ide.code_sample.adapters.CodeSampleAdapter;
import com.duy.ide.code_sample.fragments.FragmentCodeSample;
import com.duy.ide.editor.MainActivity;
import com.duy.ide.file.FileManager;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;


public class CodeSampleActivity extends AbstractAppCompatActivity implements CodeSampleAdapter.OnCodeClickListener {

    final String TAG = getClass().getSimpleName();

    private final String[] categories;
    ViewPager viewPager;
    TabLayout tabLayout;
    MaterialSearchView searchView;
    Toolbar toolbar;
    private FileManager fileManager;
    private CodePagerAdapter pagerAdapter;

    public CodeSampleActivity() {
        categories = new String[]{"Basic", "System", "Crt", "Dos", "Graph", "Math",
                "Complete_Program", "Android",
                "Android_Dialog", "Android_ZXing", "Android_Location"};
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAnalytics.getInstance(getApplicationContext()).logEvent("open_code_sample", new Bundle());

        fileManager = new FileManager(getApplicationContext());

        setContentView(R.layout.activity_code_sample);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.code_sample);

        final ArrayList<PageDescriptor> pages = new ArrayList<>();
        for (String category : categories) {
            pages.add(new SimplePageDescriptor(category, category));
        }

        pagerAdapter = new CodePagerAdapter(getSupportFragmentManager(), pages);
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                FragmentCodeSample fragmentCodeSample = pagerAdapter.getCurrentFragment();
                if (fragmentCodeSample != null) {
                    fragmentCodeSample.query(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_code_sample, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onPlay(String code) {
        //create file temp
        fileManager.setContentFileTemp(code);

        //set file temp for generate
        Intent intent = new Intent(this, ExecuteActivity.class);

        //this code is verified, do not need compile
        intent.putExtra(CompileManager.FILE_PATH, fileManager.getTempFile().getPath());
        startActivity(intent);
    }

    @Override
    public void onEdit(String code) {
        //create file temp
        String file = fileManager.createNewFileInMode("sample_" + Integer.toHexString((int) System.currentTimeMillis()) + ".pas");
        fileManager.saveFile(file, code);

        //set file temp for generate
        Intent intent = new Intent(this, MainActivity.class);

        //this code is verified, do not need compile
        intent.putExtra(CompileManager.FILE_PATH, file);
        startActivity(intent);
    }


}
